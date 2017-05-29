package me.derekzeng.radio;

import android.app.IntentService;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class PlayerService extends IntentService {
    private static final String tag = "PlayerService";
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final String ACTION_START_RADIO = "me.derekzeng.radio.action.START";

    private int maxInputSize = 100 * 1024; // 100kB
    public boolean keepRunning = true;
    private Thread thread = null;
    private AudioTrack player;

    public PlayerService() {
        super("PlayerService");

        player = new AudioTrack.Builder()
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
                .setAudioFormat(new AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(44100)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO).build())
                .setBufferSizeInBytes(maxInputSize).build();


    }

    private void startStreamingAndPlayRadio(String radioUrl) {
        player.play();
        thread = new Thread(()-> {
            Log.v(tag, "Start runnable");
            MediaCodec codec = null;
            MediaFormat format = null;
            MediaExtractor extractor = new MediaExtractor();
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            try {
                extractor.setDataSource(radioUrl);
                int numTracks = extractor.getTrackCount();
                if (numTracks == 0) return;
                extractor.selectTrack(0);
                format = extractor.getTrackFormat(0);
                format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectHE);
                format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, maxInputSize);
                codec = MediaCodec.createByCodecName("OMX.google.aac.decoder");

                codec.configure(format, null, null, 0);
                codec.start();

                while (keepRunning) {
                    int bInIndex = codec.dequeueInputBuffer(0);
                    if (bInIndex>=0) {
                        ByteBuffer bb = codec.getInputBuffer(bInIndex);
                        int size = extractor.readSampleData(bb, 0);
                        long presentationTimeUs = extractor.getSampleTime();

                        if (size > 0) {
                            codec.queueInputBuffer(bInIndex, 0, size, presentationTimeUs, 0);
                        }
                        extractor.advance();
                    }
                    int bOutIndex = codec.dequeueOutputBuffer(info, 0);
                    if (bOutIndex>=0) {
                        ByteBuffer bb = codec.getOutputBuffer(bOutIndex);
                        player.write(bb, info.size, AudioTrack.WRITE_BLOCKING);
                        codec.releaseOutputBuffer(bOutIndex, false);
                    }
                }
            } catch (IOException e) {
                Log.e(tag, "Runnable", e);
            } finally {
                Log.v(tag, "Quitting");
                if (codec!=null) {
                    codec.stop();
                    codec.release();
                }
                extractor.release();
            }
        });
        thread.start();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.v(tag, intent.getDataString());
        startStreamingAndPlayRadio(intent.getDataString());
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
