package ru.krivocraft.kbmp;

import android.media.MediaPlayer;
import android.os.SystemClock;
import android.support.v4.media.session.PlaybackStateCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class PlaybackManager implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {

    private MediaPlayer player;

    private int playerState;

    private PlayerStateCallback playerStateCallback;
    private PlaylistUpdateCallback playlistUpdateCallback;

    private String cache;
    private List<String> tracks;
    private int cursor = 0;

    PlaybackManager() {
        this.tracks = new ArrayList<>();
        this.playerState = PlaybackStateCompat.STATE_NONE;
        updatePlaybackState();
    }

    boolean isPlaying() {
        return player != null && player.isPlaying();
    }

    void play() {
        if (cursor >= 0) {
            String selectedTrack = tracks.get(cursor);
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
                    player.setDataSource(selectedTrack);
                    player.prepareAsync();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                cache = selectedTrack;

                return;
            }

            if (player != null) {
                player.start();
                playerState = PlaybackStateCompat.STATE_PLAYING;
                updatePlaybackState();
            }
        }
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
        if (index >= 0 && index < tracks.size()) {
            pause();

            cursor = index;

            if (playerStateCallback != null) {
                playerStateCallback.onTrackChanged(tracks.get(cursor));
            }

            play();
        }
    }

    void nextTrack() {
        newTrack(cursor + 1);
    }

    void previousTrack() {
        newTrack(cursor - 1);
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

    void setTrackList(List<String> trackList) {
        if (trackList != this.tracks) {
            this.tracks = trackList;
            if (playlistUpdateCallback != null) {
                playlistUpdateCallback.onPlaylistUpdated(tracks);
            }
        }
    }

    void shuffleTrackList() {
        if (tracks != null) {
            Collections.shuffle(tracks);

            cursor = tracks.indexOf(cache);

            if (playlistUpdateCallback != null) {
                playlistUpdateCallback.onPlaylistUpdated(tracks);
            }
        }
    }

    void setPlayerStateCallback(PlayerStateCallback playerStateCallback) {
        this.playerStateCallback = playerStateCallback;
    }

    void setPlaylistUpdateCallback(PlaylistUpdateCallback playlistUpdateCallback) {
        this.playlistUpdateCallback = playlistUpdateCallback;
    }

    List<String> getTrackList() {
        return tracks;
    }

    int getCurrentStreamPosition() {
        return player != null ? player.getCurrentPosition() : 0;
    }

    String getCurrentTrack() {
        return tracks.get(cursor);
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

        playerState = PlaybackStateCompat.STATE_PLAYING;
        updatePlaybackState();
    }


    interface PlayerStateCallback {
        void onPlaybackStateChanged(PlaybackStateCompat stateCompat);

        void onTrackChanged(String track);
    }

    interface PlaylistUpdateCallback {
        void onPlaylistUpdated(List<String> list);
    }

}
