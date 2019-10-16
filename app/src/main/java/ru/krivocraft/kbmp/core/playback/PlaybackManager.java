package ru.krivocraft.kbmp.core.playback;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.SystemClock;
import android.support.v4.media.session.PlaybackStateCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ru.krivocraft.kbmp.core.audiofx.EqualizerManager;
import ru.krivocraft.kbmp.core.storage.PreferencesManager;
import ru.krivocraft.kbmp.core.storage.TracksStorageManager;
import ru.krivocraft.kbmp.core.track.Track;
import ru.krivocraft.kbmp.core.track.TrackList;
import ru.krivocraft.kbmp.core.track.TrackReference;

import static android.content.Context.MODE_PRIVATE;

class PlaybackManager implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {

    private final SharedPreferences settings;
    private final AudioManager audioManager;
    private final MediaPlayer player;

    private final TracksStorageManager tracksStorageManager;
    private final EqualizerManager equalizerManager;

    private int playerState;

    private PlayerStateCallback playerStateCallback;
    private PlaylistUpdateCallback playlistUpdateCallback;

    private AudioManager.OnAudioFocusChangeListener focusChangeListener = focusChange -> {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_LOSS:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                setVolume(0.3f);
                break;
            case AudioManager.AUDIOFOCUS_GAIN:
                play();
                setVolume(1.0f);
                break;
            default:
                //Do nothing
                break;
        }
    };

    private TrackReference cache;

    private TrackList trackList;

    private int cursor = 0;

    PlaybackManager(Context context) {
        this.player = new MediaPlayer();
        this.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        this.playerState = PlaybackStateCompat.STATE_NONE;
        this.tracksStorageManager = new TracksStorageManager(context);
        this.settings = context.getSharedPreferences(PreferencesManager.STORAGE_SETTINGS, MODE_PRIVATE);
        this.equalizerManager = new EqualizerManager(player.getAudioSessionId(), context);
        updatePlaybackState();
        restoreAll();
    }

    private void requestAudioFocus() {
        if (audioManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                AudioAttributes playbackAttributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build();
                AudioFocusRequest focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                        .setAudioAttributes(playbackAttributes)
                        .setAcceptsDelayedFocusGain(false)
                        .setOnAudioFocusChangeListener(focusChangeListener)
                        .build();

                audioManager.requestAudioFocus(focusRequest);
            } else {
                audioManager.requestAudioFocus(focusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            }
        }
    }

    private boolean isPlaying() {
        return player != null && player.isPlaying();
    }

    void play() {
        if (cursor >= 0) {
            TrackReference selectedReference = getTracks().get(cursor);
            boolean mediaChanged = (cache == null || !cache.equals(selectedReference));
            Track selectedTrack = tracksStorageManager.getTrack(selectedReference);

            requestAudioFocus();

            if (mediaChanged) {

                this.player.reset();
                this.player.setOnCompletionListener(this);
                this.player.setOnPreparedListener(this);

                try {
                    player.setDataSource(selectedTrack.getPath());
                    player.prepareAsync();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                cache = selectedReference;
                return;
            }

            player.start();

            selectedTrack.setPlaying(true);
            tracksStorageManager.updateTrack(selectedTrack);

            playerState = PlaybackStateCompat.STATE_PLAYING;
            updatePlaybackState();
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

        if (previousTrackExists()) {
            Track selectedTrack = tracksStorageManager.getTrack(cache);
            selectedTrack.setPlaying(false);
            tracksStorageManager.updateTrack(selectedTrack);
        }
        audioManager.abandonAudioFocus(focusChangeListener);
        playerState = PlaybackStateCompat.STATE_PAUSED;
        updatePlaybackState();
    }

    void newTrack(int index) {
        int loopType = settings.getInt(TrackList.LOOP_TYPE, TrackList.LOOP_TRACK_LIST);

        int cursor = index;
        if (loopType == TrackList.LOOP_TRACK_LIST) {
            if (index < 0) cursor = trackList.size() - 1;
            if (index >= getTracks().size()) cursor = 0;
        }

        if (cursor >= 0 && cursor < getTracks().size()) {
            pause();

            removeTrackSelection();

            this.cursor = cursor;

            TrackReference selectedReference = getTracks().get(this.cursor);
            Track selectedTrack = tracksStorageManager.getTrack(selectedReference);
            selectedTrack.setSelected(true);
            tracksStorageManager.updateTrack(selectedTrack);

            if (playerStateCallback != null) {
                playerStateCallback.onTrackChanged(tracksStorageManager.getTrack(getTracks().get(this.cursor)));
            }

            play();
        }
    }

    private void removeTrackSelection() {
        if (previousTrackExists()) {
            Track oldSelectedTrack = tracksStorageManager.getTrack(cache);
            oldSelectedTrack.setPlaying(false);
            oldSelectedTrack.setSelected(false);
            tracksStorageManager.updateTrack(oldSelectedTrack);
        }
    }

    private boolean previousTrackExists() {
        return cache != null;
    }

    private void restoreAll() {
        List<Track> tracks = tracksStorageManager.getTrackStorage();
        for (Track track : tracks) {
            track.setSelected(false);
            track.setPlaying(false);
        }
        tracksStorageManager.updateTrackStorage(tracks);
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
            audioManager.abandonAudioFocus(focusChangeListener);

            removeTrackSelection();

            player.stop();
            playerState = PlaybackStateCompat.STATE_STOPPED;
            updatePlaybackState();
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
        if (getTracks() != null && getTracks().size() > 0) {
            return getTracks().get(cursor);
        }
        return null;
    }

    private void updatePlaybackState() {
        if (playerStateCallback != null) {
            long availableActions = PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS | PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID | PlaybackStateCompat.ACTION_SEEK_TO | PlaybackStateCompat.ACTION_STOP;

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
        int loopType = settings.getInt(TrackList.LOOP_TYPE, TrackList.LOOP_TRACK_LIST);
        switch (loopType) {
            case TrackList.LOOP_TRACK:
                newTrack(getCursor());
                break;
            case TrackList.LOOP_TRACK_LIST:
                if (getCursor() + 1 < getTrackList().size()) {
                    nextTrack();
                } else {
                    newTrack(0);
                }
                break;
            case TrackList.NOT_LOOP:
                if (getCursor() < getTrackList().size()) {
                    nextTrack();
                } else {
                    audioManager.abandonAudioFocus(focusChangeListener);
                }
                break;
            default:
                //Do nothing
                break;
        }
    }

    private void setVolume(float volume) {
        player.setVolume(volume, volume);
    }

    void destroy() {
        equalizerManager.destroy();

        audioManager.abandonAudioFocus(focusChangeListener);
        player.release();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        player.start();
        TrackReference selectedReference = getTracks().get(cursor);
        Track selectedTrack = tracksStorageManager.getTrack(selectedReference);
        selectedTrack.setPlaying(true);
        tracksStorageManager.updateTrack(selectedTrack);

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
