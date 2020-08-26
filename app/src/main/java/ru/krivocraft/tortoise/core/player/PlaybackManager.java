/*
 * Copyright (c) 2019 Nikifor Fedorov
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *     SPDX-License-Identifier: Apache-2.0
 *     Contributors:
 * 	    Nikifor Fedorov - whole development
 */

package ru.krivocraft.tortoise.core.player;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.SystemClock;
import android.support.v4.media.session.PlaybackStateCompat;
import ru.krivocraft.tortoise.core.api.AudioFocus;
import ru.krivocraft.tortoise.core.api.settings.ReadOnlySettings;
import ru.krivocraft.tortoise.core.api.settings.Settings;
import ru.krivocraft.tortoise.core.base.AndroidAudioFocus;
import ru.krivocraft.tortoise.core.base.SharedPreferencesSettings;
import ru.krivocraft.tortoise.core.model.Track;
import ru.krivocraft.tortoise.core.model.TrackList;
import ru.krivocraft.tortoise.core.model.TrackReference;
import ru.krivocraft.tortoise.core.rating.Shuffle;
import ru.krivocraft.tortoise.core.settings.SettingsStorageManager;
import ru.krivocraft.tortoise.core.tracklist.TracksStorageManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class PlaybackManager implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, AudioFocus.ChangeListener {

    private final MediaPlayer player;

    private final TracksStorageManager tracksStorageManager;

    private int playerState;

    private final PlayerStateCallback playerStateCallback;
    private final PlaylistUpdateCallback playlistUpdateCallback;

    private final AudioFocus focus;
    private final ReadOnlySettings settings;

    private TrackReference cache;

    private TrackList trackList;

    private int cursor = 0;

    PlaybackManager(Context context, PlayerStateCallback playerStateCallback, PlaylistUpdateCallback playlistUpdateCallback) {
        this.playerStateCallback = playerStateCallback;
        this.playlistUpdateCallback = playlistUpdateCallback;

        this.player = new MediaPlayer();
        this.trackList = TrackList.EMPTY;

        this.playerState = PlaybackStateCompat.STATE_NONE;
        this.tracksStorageManager = new TracksStorageManager(context);

        this.focus = new AndroidAudioFocus(this, (AudioManager) context.getSystemService(Context.AUDIO_SERVICE));
        this.settings = new SharedPreferencesSettings();

        updatePlaybackState();
        restoreAll();
    }

    private boolean isPlaying() {
        return player != null && player.isPlaying();
    }

    void play() {
        if (cursor >= 0 && getTracks().size() > 0) {
            TrackReference selectedReference = getTracks().get(cursor);
            boolean mediaChanged = (cache == null || !cache.equals(selectedReference));
            Track selectedTrack = tracksStorageManager.getTrack(selectedReference);

            focus.request();

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
        focus.release();
        playerState = PlaybackStateCompat.STATE_PAUSED;
        updatePlaybackState();
    }

    void newTrack(int index) {
        int cursor = index;
        cursor = replaceCursorIfOutOfBounds(index, cursor);

        if (!cursorOutOfBounds(cursor)) {
            pause();
            deselectCurrentTrack();
            this.cursor = cursor;
            if (!settings.read(SettingsStorageManager.KEY_SHOW_IGNORED, false) && tracksStorageManager.getTrack(getSelectedTrackReference()).isIgnored()) {
                nextTrack();
                return;
            }
            selectCurrentTrack();
            playerStateCallback.onTrackChanged(tracksStorageManager.getTrack(getSelectedTrackReference()));
            play();
        }
    }

    private int replaceCursorIfOutOfBounds(int index, int cursor) {
        if (index < 0) return trackList.size() - 1;
        if (index >= getTracks().size()) return 0;
        return cursor;
    }

    private void selectCurrentTrack() {
        TrackReference selectedReference = getTracks().get(this.cursor);
        Track selectedTrack = tracksStorageManager.getTrack(selectedReference);
        selectedTrack.setSelected(true);
        tracksStorageManager.updateTrack(selectedTrack);
    }

    private boolean cursorOutOfBounds(int cursor) {
        return cursor < 0 || cursor >= getTracks().size();
    }

    private void deselectCurrentTrack() {
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
        cursor = trackList.shuffle(new Shuffle(tracksStorageManager, settings), getSelectedTrackReference());
        playlistUpdateCallback.onPlaylistUpdated(trackList);
    }

    int getCursor() {
        return cursor;
    }

    void proceed() {
        int loopType = settings.read(TrackList.LOOP_TYPE, TrackList.LOOP_TRACK_LIST);
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
                if (getCursor() < getTrackList().size() - 1) {
                    nextTrack();
                } else {
                    stop();

                }
                break;
            default:
                break;
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
            focus.release();

            deselectCurrentTrack();
            cache = null;

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

    TrackList getTrackList() {
        return trackList;
    }

    int getCurrentStreamPosition() {
        return player != null ? player.getCurrentPosition() : 0;
    }

    TrackReference getSelectedTrackReference() {
        if (getTracks() != null && getTracks().size() > 0) {
            return getTracks().get(cursor);
        }
        return null;
    }

    private void updatePlaybackState() {
        if (playerStateCallback != null) {
            long availableActions = PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                    PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID |
                    PlaybackStateCompat.ACTION_SEEK_TO |
                    PlaybackStateCompat.ACTION_STOP;

            if (playerState == PlaybackStateCompat.STATE_PLAYING) {
                availableActions |= PlaybackStateCompat.ACTION_PAUSE;
            } else if (playerState == PlaybackStateCompat.STATE_PAUSED) {
                availableActions |= PlaybackStateCompat.ACTION_PLAY;
            }

            PlaybackStateCompat.Builder builder = new PlaybackStateCompat.Builder()
                    .setActions(availableActions)
                    .setState(playerState, getCurrentStreamPosition(), 1, SystemClock.elapsedRealtime());
            playerStateCallback.onPlaybackStateChanged(builder.build());
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        proceed();
    }

    private void setVolume(float volume) {
        player.setVolume(volume, volume);
    }

    void destroy() {
        focus.release();
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

    @Override
    public void mute() {
        pause();
    }

    @Override
    public void gain() {
        play();
        setVolume(1.0f);
    }

    @Override
    public void silently() {
        setVolume(0.3f);
    }

    interface PlaylistUpdateCallback {
        void onPlaylistUpdated(TrackList list);
    }

}
