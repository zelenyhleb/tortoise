package ru.krivocraft.kbmp;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class PlayerService extends Service implements MediaPlayer.OnPreparedListener {

    private static MediaPlayer player;
    private Binder mBinder = new LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
    }

    public class LocalBinder extends Binder {
        public PlayerService getServerInstance() {
            return PlayerService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public void startPlaying(String file, int progress) throws IOException {
        player = new MediaPlayer();
        player.setDataSource(file);
        player.prepare();
        player.start();
    }

    public int stopPlaying() {
        int currentPosition = player.getCurrentPosition();
        release();
        return currentPosition;
    }

    private void release() {
        player.release();
        player = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
