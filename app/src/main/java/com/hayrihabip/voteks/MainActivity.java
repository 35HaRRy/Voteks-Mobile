package com.hayrihabip.voteks;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import android.os.Bundle;
import android.app.Activity;
import android.os.StrictMode;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Properties;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        try
        {
            JSch jsch = new JSch();
            Session session = jsch.getSession("PIXELSOFTOFFICE\\hayri", "192.168.1.5", 22);
            session.setPassword("@Ironmanmark42");

            // Avoid asking for key confirmation
            Properties prop = new Properties();
            prop.put("StrictHostKeyChecking", "no");
            session.setConfig(prop);

            session.connect();

            ChannelExec channel = (ChannelExec)session.openChannel("exec");
            channel.setCommand("ls ~/");

            channel.setInputStream(null);
            channel.setErrStream(null);

            InputStream in = channel.getInputStream();
            InputStream err = channel.getErrStream();
            OutputStream out = channel.getOutputStream();

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

            Log.v("SSH", "SSH - " + sb.toString());

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            out.write(bytes.toByteArray());
            Log.v("SSH", "SSH - out: " + bytes.toString());

            // SSH Channel
            /*ByteArrayOutputStream baos = new ByteArrayOutputStream();
            channelSSH.setOutputStream(baos);

            // Execute command
            channelSSH.setCommand("dir");

            channelSSH.connect();
            channelSSH.disconnect();

            Log.v("SSH", "SSH - ExitStatus: " + channelSSH.getExitStatus());
            Log.v("SSH", "SSH - " + baos.toString());*/

        }
        catch (Exception e)
        {
            Log.e("SSH", "SSH: " + e.getMessage());
        }
    }
}