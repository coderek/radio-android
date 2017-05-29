package me.derekzeng.radio;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaDataSource;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity {
    FileOutputStream fos;
    static final String radioUrl = "http://mediacorp.rastream.com/933fm";


    private static final String tag = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn = (Button) findViewById(R.id.button);
        btn.setOnClickListener((view)->{
            playRadio();
        });
    }

    void playRadio() {
        Intent i = new Intent(this, PlayerService.class);
        i.setData(Uri.parse(radioUrl));
        startService(i);
    }



    byte[] buffer = new byte[16];
    int read = 0;

    class MyMediaDataSource extends MediaDataSource {
        ByteBuffer buffer;
        int current;

        MyMediaDataSource() {
            buffer = ByteBuffer.allocate(100 * 1024);
            current = 0;
        }


        public void write(MediaCodec.BufferInfo info, ByteBuffer bb) {
            byte[] bytes = new byte[info.size];
            bb.get(bytes, info.offset, info.size);
            Log.v(tag, info.offset + ":"+ info.size);
            buffer.put(bytes);
        }

        public long getSize() {
            return -1;
        }

        public int readAt(long position, byte[] buf, int offset, int size) {
            for (int i=0;i<size;i++) {
                buf[i] = buffer.get(i);
            }
            return size;
        }

        public void close() {}
    }

}
