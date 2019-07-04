package ru.krivocraft.kbmp;

import android.media.MediaPlayer;
import android.os.SystemClock;
import android.support.v4.media.session.PlaybackStateCompat;

import java.io.IOException;

class PlaybackManager implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {

    private MediaPlayer player;

    private int playerState;

    private PlayerStateCallback playerStateCallback;

    private TrackList trackList;
    private Track cache;

    PlaybackManager(PlayerStateCallback playerStateCallback) {
        this.playerStateCallback = playerStateCallback;
        this.trackList = new TrackList("initial");
        this.playerState = PlaybackStateCompat.STATE_NONE;
        updatePlaybackState();
    }

    boolean isPlaying() {
        return player != null && player.isPlaying();
    }

    void play() {
        if (trackList.getCursor() >= 0) {
            Track selectedTrack = trackList.getSelectedTrack();
            boolean mediaChanged = (cache == null || !cache.equals(selectedTrack));

            if (mediaChanged) {
                if (player == null) {
                    player = new MediaPlayer();
                    player.setOnCompletionListener(this);
                    player.setOnPreparedListener(this);
                } else {
                    player.reset();
                }

                try {
                    player.setDataSource(selectedTrack.getPath());
                    player.prepareAsync();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (cache != null) {
                    cache.setSelected(false);
                }
                selectedTrack.setSelected(true);

                cache = selectedTrack;

                return;
            }

            player.start();

            selectedTrack.setPlaying(true);

            playerState = PlaybackStateCompat.STATE_PLAYING;
            updatePlaybackState();
        }
    }

    void seekTo(int position) {
        player.pause();
        player.seekTo(position);
        player.start();
    }

    void pause() {
        Track selectedTrack = trackList.getSelectedTrack();

        if (isPlaying()) {
            player.pause();
            selectedTrack.setPlaying(false);
        }

        playerState = PlaybackStateCompat.STATE_PAUSED;
        updatePlaybackState();
    }

    void newTrack(int index) {
        if (index >= 0 && index < trackList.getSize()) {
            pause();

            if (cache != null) {
                cache.setSelected(false);
            }

            trackList.setCursor(index);

            Track selectedTrack = trackList.getSelectedTrack();

            selectedTrack.setProgress(0);
            selectedTrack.setSelected(true);

            playerStateCallback.onTrackChanged(selectedTrack);

            play();
        }
    }

    void nextTrack() {
        newTrack(trackList.getCursor() + 1);
    }

    void previousTrack() {
        newTrack(trackList.getCursor() - 1);
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

    void setTrackList(TrackList trackList) {
        if (trackList != this.trackList)
            this.trackList = trackList;
    }

    TrackList getTrackList() {
        return trackList;
    }

    int getCurrentStreamPosition() {
        return player != null ? player.getCurrentPosition() : 0;
    }

    private void updatePlaybackState() {
        if (playerStateCallback != null) {
            long availableActions = PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS | PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID | PlaybackStateCompat.ACTION_SEEK_TO;

            if (playerState == PlaybackStateCompat.STATE_PLAYING) {
                availableActions |= PlaybackStateCompat.ACTION_PAUSE;
            } else if (playerState == PlaybackStateCompat.STATE_PAUSED) {
                availableActions |= PlaybackStateCompat.ACTION_PLAY;
            }

            PlaybackStateCompat.Builder builder = new PlaybackStateCompat.Builder();
            builder.setActions(availableActions).setState(playerState, getCurrentStreamPosition(), 1, SystemClock.elapsedRealtime());
            playerStateCallback.onPlaybackStateChanged(builder.build());
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        nextTrack();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        player.start();

        cache.setPlaying(true);

        playerState = PlaybackStateCompat.STATE_PLAYING;
        updatePlaybackState();
    }


    interface PlayerStateCallback {
        void onPlaybackStateChanged(PlaybackStateCompat stateCompat);

        void onTrackChanged(Track track);
    }
}
