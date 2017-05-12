package me.derekzeng.radio;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaDataSource;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity {
    MediaPlayer mp;
    FileOutputStream fos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mp = new MediaPlayer();
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mp.setOnPreparedListener(mp-> {
            mp.start();
        });

        Button btn = (Button) findViewById(R.id.button);
        btn.setOnClickListener((view)->{
            playRadio();
        });
    }

    void playRadio() {
        startService(new Intent(this, RadioStreamer.class));
    }
}
