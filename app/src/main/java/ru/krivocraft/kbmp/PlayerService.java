package ru.krivocraft.kbmp;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PlayerService extends Service implements MediaPlayer.OnPreparedListener {

    private static MediaPlayer player;
    private Binder mBinder = new LocalBinder();
    private Playlist currentPlaylist;
    private Composition currentComposition;

    private int currentCompositionProgress = 0;

    private List<OnCompositionChangedListener> listeners = new ArrayList<>();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
    }

    class LocalBinder extends Binder {
        PlayerService getServerInstance() {
            return PlayerService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        currentPlaylist = (Playlist) intent.getSerializableExtra(Constants.PLAYLIST);
        return START_STICKY;
    }

    public void start() {

        if (player != null) {
            stop();
        }

        player = new MediaPlayer();
        try {
            player.setDataSource(currentComposition.getPath());
            player.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        player.seekTo(currentCompositionProgress);
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                nextComposition();
            }
        });
        player.start();
    }

    void nextComposition() {
        newComposition(currentPlaylist.indexOf(currentComposition) + 1);
    }

    void previousComposition() {
        newComposition(currentPlaylist.indexOf(currentComposition) - 1);
    }

    void newComposition(int compositionIndex) {
        if (compositionIndex >= 0 && compositionIndex < currentPlaylist.getSize()) {
            release();
            currentCompositionProgress = 0;
            currentComposition = currentPlaylist.getComposition(compositionIndex);

            for (OnCompositionChangedListener listener : listeners) {
                listener.onCompositionChanged(currentComposition);
            }

            start();
        }
    }

    void stop() {
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

    int getProgress() {
        if (player != null) {
            return player.getCurrentPosition();
        } else {
            return 0;
        }
    }

    void setCurrentCompositionProgress(int currentCompositionProgress) {
        this.currentCompositionProgress = currentCompositionProgress;
    }

    void addListener(OnCompositionChangedListener listener) {
        listeners.add(listener);
    }

    void removeListener(OnCompositionChangedListener listener) {
        listeners.remove(listener);
    }
}
