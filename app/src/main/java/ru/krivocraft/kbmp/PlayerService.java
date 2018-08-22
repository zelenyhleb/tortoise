package ru.krivocraft.kbmp;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.io.IOException;

public class PlayerService extends Service implements MediaPlayer.OnPreparedListener {

    private static MediaPlayer player;
    private Binder mBinder = new LocalBinder();
    private Playlist currentPlaylist;
    private Composition currentComposition;

    private int currentCompositionProgress;
    private int currentCompositionIndex;

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
        currentPlaylist = (Playlist) intent.getSerializableExtra(Constants.PLAYLIST);
        setCurrentComposition(0);
        return START_STICKY;
    }

    public void startPlaying() throws IOException {

        if (player != null) {
            stopPlaying();
        }

        player = new MediaPlayer();
        player.setDataSource(currentComposition.getPath());
        player.prepare();
        player.seekTo(currentCompositionProgress);
        player.start();
    }

    public void stopPlaying() {
        if (player != null) {
            currentCompositionProgress = player.getCurrentPosition();
            player.stop();
            release();
        }
    }

    private void release() {
        player.release();
        player = null;
    }

    public int getProgress() {
        if (player != null) {
            return player.getCurrentPosition();
        } else {
            return 0;
        }
    }

    public void setCurrentComposition(int index) {
        this.currentComposition = currentPlaylist.getComposition(index);
    }

    public void setCurrentCompositionProgress(int currentCompositionProgress) {
        this.currentCompositionProgress = currentCompositionProgress;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
