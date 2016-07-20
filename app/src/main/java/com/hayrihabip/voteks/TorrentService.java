package com.hayrihabip.voteks;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.gson.Gson;
import com.hayrihabip.voteks.data.Constants;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class TorrentService extends IntentService {
    private Session session;

    public TorrentService() {
        super("TorrentService");

        Log.v("Torrent", "Torrent started");

        try
        {
            JSch jsch = new JSch();
            session = jsch.getSession("osmc", "192.168.0.19", 22);
            session.setPassword("osmc");

            // Avoid asking for key confirmation
            Properties prop = new Properties();
            prop.put("StrictHostKeyChecking", "no");
            session.setConfig(prop);

            session.connect();
        } catch (Exception e) {
            Log.e("SSH", "SSH: " + e.getMessage());
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();

            try {
                if (Constants.ACTION_SEARCH.equals(action))
                    handleActionSearch(intent.getExtras().get(Constants.EXTRA_SEARCH).toString());
                else if (Constants.ACTION_DOWNLOAD.equals(action))
                    handleActionDownload(intent.getStringExtra(Constants.EXTRA_DOWNLOAD));
            }  catch (Exception e) {
                Log.e("SSH", "SSH - Error: " + e.getMessage());
            }
        }
    }

    private void handleActionSearch(String search) throws IOException, JSchException {
        Log.v("Torrent", "Torrent action: Search");

        //String result = ExecuteCommand("cd /media/SAMSUNG/Torrent && python asskick.py -s \"" + search + "\"").replace("u'", "'");
        String result = Search(search);

        Intent intent = new Intent("FromTorrentService");
        intent.putExtra(Constants.EXTRA_TORRENTRESULTS, result);
        intent.putExtra(Constants.EXTRA_SEARCH, search);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    private void handleActionDownload(String download) throws IOException, JSchException {
        Log.v("Torrent", "Torrent action: Download");

        String result = ExecuteCommand("cd /media/SAMSUNG/Torrent && python asskick.py -d \"" + download + "\"");
    }

    public static String Search(String filter) {
        String result = "";

        try {
            Document document = Jsoup.connect("https://kat.cr/usearch/" + filter + "/").get();

            if (document.select("[data-download]").size() > 0) {
                List<Map<String, String>> torrents = new ArrayList<>();

                Elements rows = document.select("table.data tr:not(.firstr)");
                for (Element row: rows) {
                    Map<String, String> torrent = new HashMap<>();
                    torrent.put("href", row.select("[title='Download torrent file']").attr("href"));
                    torrent.put("size", row.select("td:eq(1)").text());
                    torrent.put("title", row.select("a.cellMainLink").text());
                    torrent.put("age", row.select("td:eq(3)").text());
                    torrent.put("seeders", row.select("td:eq(4)").text());
                    torrent.put("leechers", row.select("td:eq(5)").text());

                    torrents.add(torrent);
                }

                result = new Gson().toJson(torrents);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }
    private String ExecuteCommand(String command) throws IOException, JSchException {
        ChannelExec channel = (ChannelExec)session.openChannel("exec");
        channel.setCommand(command);

        Log.v("Torrent", "Torrent SSH command: " + command);

        channel.setInputStream(null);
        channel.setErrStream(null);

        InputStream in = channel.getInputStream();
        InputStream err = channel.getErrStream();

        channel.connect();

        int exitCode = 0;
        while(true) {
            if(channel.isClosed()){
                exitCode = channel.getExitStatus();
                break;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                Log.v("SSH", "SSH - ie: " + ie.toString());
            }
        }

        BufferedReader inReader = new BufferedReader(new InputStreamReader(in));
        BufferedReader errReader = new BufferedReader(new InputStreamReader(err));

        String s;
        StringBuilder sb = new StringBuilder();

        if (exitCode != 0) {
            while ((s = errReader.readLine()) != null) {
                sb.append(s).append("\n");
            }

            //status.setData(sb.toString());
        } else {
            while ((s = inReader.readLine()) != null) {
                sb.append(s).append("\n");
            }
        }

        //Log.v("SSH", "SSH - " + sb.toString());
        return sb.toString();
    }
}