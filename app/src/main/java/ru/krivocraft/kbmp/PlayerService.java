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
    private int currentCompositionIndex = 0;

    private List<OnCompositionChangedListener> listeners = new ArrayList<>();

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
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                nextComposition();
            }
        });
        player.start();
    }

    public void nextComposition() {
        if (currentCompositionIndex + 1 < currentPlaylist.getSize()) {
            release();
            currentCompositionProgress = 0;
            currentCompositionIndex++;
            currentComposition = currentPlaylist.getComposition(currentCompositionIndex);

            for (OnCompositionChangedListener listener : listeners) {
                listener.onCompositionChanged(currentComposition);
            }
            try {
                startPlaying();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void previousComposition() {
        if (currentCompositionIndex > 0) {
            release();
            currentCompositionProgress = 0;
            currentCompositionIndex--;
            currentComposition = currentPlaylist.getComposition(currentCompositionIndex);

            for (OnCompositionChangedListener listener : listeners) {
                listener.onCompositionChanged(currentComposition);
            }
            try {
                startPlaying();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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

    public void addListener(OnCompositionChangedListener listener) {
        listeners.add(listener);
    }
}
