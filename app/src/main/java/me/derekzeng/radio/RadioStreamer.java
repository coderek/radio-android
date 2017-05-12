package me.derekzeng.radio;

import android.app.IntentService;
import android.app.Notification;
import android.content.Intent;
import android.media.MediaDataSource;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by coderek on 11/05/17.
 */
public class RadioStreamer extends IntentService {

    private final int ONGOING_NOTIFICATION_ID = 1;
    private MediaPlayer mp = new MediaPlayer();
    public RadioStreamer() {
        super("Radio Service");
    }

    @Override
    public void onHandleIntent(Intent i) {
        Log.d("RadioStreamer", "Starting Radio Service");
        mp.setOnPreparedListener(mp-> mp.start());

        InputStream in = null;
        HttpURLConnection conn = null;
        try {
            URL u = new URL("http://mediacorp.rastream.com/933fm");
            conn = (HttpURLConnection) u.openConnection();
        } catch (IOException e) {
            return;
        }
        if (conn == null) return;

        try {
            in = conn.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        mp.setDataSource(new RadioSource(in));
        mp.prepareAsync();
    }



    class RadioSource extends MediaDataSource {

        InputStream in;
        public RadioSource (InputStream in) {
            super();

            this.in = in;
        }
        public synchronized long getSize () { return -1;};
        public synchronized int readAt (long position,
                           byte[] buffer,
                           int offset,
                           int size) {

            try {
                int i = in.read(buffer, offset, size);
                Log.d("radio", i+"");
                return i;
            } catch (Exception e) {
                return -1;
            }
        }

        public synchronized void close () {
            try {
                if (in != null) in.close();
            } catch (IOException e) {
            }
        }
    }
}
