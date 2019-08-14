package ru.krivocraft.kbmp;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.SystemClock;
import android.support.v4.media.session.PlaybackStateCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ru.krivocraft.kbmp.constants.Constants;

import static android.content.Context.MODE_PRIVATE;

class PlaybackManager implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {

    private MediaPlayer player;
    private Context context;

    private int playerState;

    private PlayerStateCallback playerStateCallback;
    private PlaylistUpdateCallback playlistUpdateCallback;

    private TrackReference cache;

    private TrackList trackList;

    private int cursor = 0;

    PlaybackManager(Context context) {
        this.context = context;
        this.playerState = PlaybackStateCompat.STATE_NONE;
        updatePlaybackState();
    }

    private boolean isPlaying() {
        return player != null && player.isPlaying();
    }

    void play() {
        if (cursor >= 0) {
            TrackReference selectedReference = getTracks().get(cursor);
            boolean mediaChanged = (cache == null || !cache.equals(selectedReference));
            Track selectedTrack = Tracks.getTrack(context, selectedReference);

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

                cache = selectedReference;
                return;
            }

            if (player != null) {
                player.start();

                selectedTrack.setPlaying(true);
                Tracks.updateTrack(context, selectedReference, selectedTrack);

                playerState = PlaybackStateCompat.STATE_PLAYING;
                updatePlaybackState();
            }
        }
    }

    void seekTo(int position) {
        pause();
        player.seekTo(position);
        play();
    }

    void pause() {
        if (isPlaying()) {
            player.pause();
        }

        if (trackList != null) {
            TrackReference selectedReference = trackList.get(cursor);
            Track selectedTrack = Tracks.getTrack(context, selectedReference);
            selectedTrack.setPlaying(false);
            Tracks.updateTrack(context, selectedReference, selectedTrack);
        }

        playerState = PlaybackStateCompat.STATE_PAUSED;
        updatePlaybackState();
    }

    void newTrack(int index) {
        if (index >= 0 && index < getTracks().size()) {
            pause();

            TrackReference oldSelectedReference = getTracks().get(cursor);
            Track oldSelectedTrack = Tracks.getTrack(context, oldSelectedReference);
            oldSelectedTrack.setPlaying(false);
            oldSelectedTrack.setSelected(false);
            Tracks.updateTrack(context, oldSelectedReference, oldSelectedTrack);

            cursor = index;

            TrackReference selectedReference = getTracks().get(cursor);
            Track selectedTrack = Tracks.getTrack(context, selectedReference);
            selectedTrack.setSelected(true);
            Tracks.updateTrack(context, selectedReference, selectedTrack);

            if (playerStateCallback != null) {
                playerStateCallback.onTrackChanged(Tracks.getTrack(context, getTracks().get(cursor)));
            }

            play();
        }
    }

    private void restoreAll(){
        List<Track> tracks = Tracks.getTrackStorage(context);
        for (Track track : tracks) {
            track.setSelected(false);
            track.setPlaying(false);
        }
        Tracks.updateTrackStorage(context, tracks);
    }

    void shuffle() {
        cursor = trackList.shuffle(getCurrentTrack());
        playlistUpdateCallback.onPlaylistUpdated(trackList);
    }

    int getCursor() {
        return cursor;
    }

    void nextTrack() {
        newTrack(cursor + 1);
    }

    void previousTrack() {
        newTrack(cursor - 1);
    }

    void stop() {
        if (player != null) {
            restoreAll();

            player.stop();
            playerState = PlaybackStateCompat.STATE_STOPPED;
            updatePlaybackState();
            player.release();
            player = null;
        }
    }

    void setTrackList(TrackList trackList, boolean sendUpdate) {
        if (trackList != this.trackList) {
            this.trackList = trackList;
            if (sendUpdate && playlistUpdateCallback != null) {
                playlistUpdateCallback.onPlaylistUpdated(trackList);
            }
        }
    }

    void setCursor(int cursor) {
        this.cursor = cursor;
    }

    private List<TrackReference> getTracks() {
        if (getTrackList() != null)
            return getTrackList().getTrackReferences();
        else
            return new ArrayList<>();
    }

    void setPlayerStateCallback(PlayerStateCallback playerStateCallback) {
        this.playerStateCallback = playerStateCallback;
    }

    void setPlaylistUpdateCallback(PlaylistUpdateCallback playlistUpdateCallback) {
        this.playlistUpdateCallback = playlistUpdateCallback;
    }

    TrackList getTrackList() {
        return trackList;
    }

    int getCurrentStreamPosition() {
        return player != null ? player.getCurrentPosition() : 0;
    }

    TrackReference getCurrentTrack() {
        if (getTracks() != null)
            return getTracks().get(cursor);
        else
            return null;
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
        SharedPreferences preferences = context.getSharedPreferences(Constants.STORAGE_SETTINGS, MODE_PRIVATE);
        int loopType = preferences.getInt(Constants.LOOP_TYPE, Constants.NOT_LOOP);
        switch (loopType) {
            case Constants.LOOP_TRACK:
                newTrack(getCursor());
                break;
            case Constants.LOOP_TRACK_LIST:
                if (getCursor() + 1 < getTrackList().size()) {
                    nextTrack();
                } else {
                    newTrack(0);
                }
                break;
            case Constants.NOT_LOOP:
                if (getCursor() < getTrackList().size()) {
                    nextTrack();
                }
                break;
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        player.start();
        TrackReference selectedReference = getTracks().get(cursor);
        Track selectedTrack = Tracks.getTrack(context, selectedReference);
        selectedTrack.setPlaying(true);
        Tracks.updateTrack(context, selectedReference, selectedTrack);

        playerState = PlaybackStateCompat.STATE_PLAYING;
        updatePlaybackState();
    }


    interface PlayerStateCallback {
        void onPlaybackStateChanged(PlaybackStateCompat stateCompat);

        void onTrackChanged(Track track);
    }

    interface PlaylistUpdateCallback {
        void onPlaylistUpdated(TrackList list);
    }

}
