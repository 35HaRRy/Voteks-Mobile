package com.hayrihabip.voteks;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import android.os.Bundle;
import android.app.Activity;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

public class MainActivity extends Activity {
    Session session;

    EditText txtCommand;
    TextView txtResult;

    ChannelExec channel;

    InputStream in;
    InputStream err;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtCommand = (EditText)findViewById(R.id.txtCommand);
        txtResult = (TextView)findViewById(R.id.txtResult);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

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

            channel = (ChannelExec) session.openChannel("exec");

            channel.setInputStream(null);
            channel.setErrStream(null);

            in = channel.getInputStream();
            err = channel.getErrStream();
        } catch (Exception e) {
            Log.e("SSH", "SSH: " + e.getMessage());
        }
    }

    public void btnSend_Click(View v) {
        Log.v("SSH", "SSH executed");

        try {
            txtResult.setText("SSH - Result: " + ExecuteCommand(txtCommand.getText().toString()));
        } catch (Exception e) {
            txtResult.setText("SSH - Error: " + e.getMessage());
        }
    }

    private String ExecuteCommand(String command) throws IOException, JSchException {
        ChannelExec channel = (ChannelExec)session.openChannel("exec");
        channel.setCommand(command);

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