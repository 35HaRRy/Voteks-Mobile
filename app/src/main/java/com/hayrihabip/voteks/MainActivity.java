package com.hayrihabip.voteks;

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
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

public class MainActivity extends Activity {
    EditText txtCommand;
    TextView txtResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtCommand = (EditText)findViewById(R.id.txtCommand);
        txtResult = (TextView)findViewById(R.id.txtResult);

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mTorrentReceiver, new IntentFilter("FromTorrentService"));
    }

    private BroadcastReceiver mTorrentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                JSONArray torrents = new JSONArray(intent.getStringExtra("Torrents"));
                ((ListView)findViewById(R.id.lvTorrents)).setAdapter();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    public void btnSend_Click(View v) {

    }
}