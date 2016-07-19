package com.hayrihabip.voteks;

import com.hayrihabip.voteks.data.Constants;
import com.hayrihabip.voteks.data.TorrentAdapter;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.app.Activity;
import android.os.StrictMode;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.Properties;

public class MainActivity extends Activity {
    EditText txtCommand;
    TextView txtResult;
    ListView lvTorrents;

    JSONArray torrents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtCommand = (EditText)findViewById(R.id.txtCommand);
        txtResult = (TextView)findViewById(R.id.txtResult);

        lvTorrents = (ListView)findViewById(R.id.lvTorrents);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("torrents")) {
                try {
                    torrents = new JSONArray(savedInstanceState.getString("torrents"));
                } catch (JSONException e) {
                    e.printStackTrace();

                    Log.e("Torrent", "Torrent parse error:" + e.getMessage());
                }
            }
        }

        if (torrents != null)
            setTorrent();

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mTorrentReceiver, new IntentFilter("FromTorrentService"));
    }

    protected void onSaveInstanceState(Bundle bundle) {
        if (torrents != null)
            bundle.putString("torrents", torrents.toString());

        super.onSaveInstanceState(bundle);
    }

    private BroadcastReceiver mTorrentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                txtCommand.setText(intent.getStringExtra(Constants.EXTRA_SEARCH));

                torrents = new JSONArray(intent.getStringExtra(Constants.EXTRA_TORRENTRESULTS));
                setTorrent();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private void setTorrent() {
        lvTorrents.setAdapter(new TorrentAdapter(MainActivity.this, torrents));
        lvTorrents.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                try {
                    Intent intent = new Intent(MainActivity.this, TorrentService.class);
                    intent.setAction(Constants.ACTION_DOWNLOAD);
                    intent.putExtra(Constants.EXTRA_DOWNLOAD, torrents.getJSONObject(position).getString("href"));

                    Log.v("Torrent", "Torrent downloading: " + torrents.getJSONObject(position).getString("href"));
                    MainActivity.this.startService(intent);
                } catch (JSONException e) {
                    e.printStackTrace();

                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT);
                }
            }
        });
    }

    public void btnSend_Click(View v) {
        Intent intent = new Intent(MainActivity.this, TorrentService.class);
        intent.setAction(Constants.ACTION_SEARCH);
        intent.putExtra(Constants.EXTRA_SEARCH, txtCommand.getText());

        Log.v("Torrent", "Torrent search: " + txtCommand.getText());

        MainActivity.this.startService(intent);
    }
}