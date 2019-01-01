package ru.krivocraft.kbmp;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.SystemClock;
import android.support.v4.media.session.PlaybackStateCompat;

import java.io.IOException;

class PlaybackManager {
    private MediaPlayer player;
    private int playerState;
    private Context context;
    private Callback callback;

    private Playlist playlist;
    private Track cache;

    PlaybackManager(Context context, Callback callback) {
        this(context, callback, new Playlist(context, "initial"));
    }

    PlaybackManager(Context context, Callback callback, Playlist playlist) {
        this.context = context;
        this.callback = callback;
        this.playlist = playlist;
    }

    boolean isPlaying() {
        if (player != null)
            return player.isPlaying();
        else
            return false;
    }

    void play() {
        Track selectedTrack = playlist.getSelectedTrack();
        boolean mediaChanged = (cache == null || !cache.equals(selectedTrack));

        if (mediaChanged) {
            if (player == null) {
                player = new MediaPlayer();
            } else {
                player.reset();
            }

            try {
                player.setDataSource(selectedTrack.getPath());
                player.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }

            cache = selectedTrack;
        }

        player.start();
        playerState = PlaybackStateCompat.STATE_PLAYING;
        updatePlaybackState();
    }

    void seekTo(int position) {
        player.pause();
        player.seekTo(position);
        player.start();
    }

    void pause() {
        if (isPlaying()) {
            player.pause();
        }
        playerState = PlaybackStateCompat.STATE_PAUSED;
        updatePlaybackState();
    }

    void newTrack(int index) {
        if (index >= 0 && index < playlist.getSize()) {
            pause();

            playlist.getSelectedTrack().setSelected(false);

            playlist.setCursor(index);
            playlist.getSelectedTrack().setProgress(0);
            callback.onTrackChanged(playlist.getSelectedTrack());

            play();
        }
    }

    void nextTrack() {
        newTrack(playlist.getCursor() + 1);
    }

    void previousTrack() {
        newTrack(playlist.getCursor() - 1);
    }

    void stop() {
        if (player != null) {
            player.stop();
            playerState = PlaybackStateCompat.STATE_STOPPED;
            updatePlaybackState();
            player.release();
            player = null;
        }
    }

    int getProgress() {
        if (player != null) {
            return player.getCurrentPosition();
        } else {
            return 0;
        }
    }

    void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
    }

    Playlist getPlaylist() {
        return playlist;
    }

    int getCurrentStreamPosition() {
        return player != null ? player.getCurrentPosition() : 0;
    }

    void updatePlaybackState() {
        if (callback != null) {
            long availableActions = PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS | PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID;

            if (isPlaying()) {
                availableActions |= PlaybackStateCompat.ACTION_PAUSE;
            } else {
                availableActions |= PlaybackStateCompat.ACTION_PLAY;
            }

            PlaybackStateCompat.Builder builder = new PlaybackStateCompat.Builder();
            builder.setActions(availableActions).setState(playerState, getCurrentStreamPosition(), 1, SystemClock.elapsedRealtime());
            callback.onPlaybackStateChanged(builder.build());
        }
    }


    interface Callback {
        void onPlaybackStateChanged(PlaybackStateCompat stateCompat);

        void onTrackChanged(Track track);
    }
}
