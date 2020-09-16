/*
 * Copyright (c) 2020 Nikifor Fedorov
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
 *         Nikifor Fedorov and others
 */

package ru.krivocraft.tortoise.android.player;

import android.content.Context;
import android.media.AudioManager;
import android.os.SystemClock;
import android.support.v4.media.session.PlaybackStateCompat;
import ru.krivocraft.tortoise.android.AndroidAudioFocus;
import ru.krivocraft.tortoise.android.AndroidMediaPlayer;
import ru.krivocraft.tortoise.core.api.AudioFocus;
import ru.krivocraft.tortoise.core.api.MediaPlayer;
import ru.krivocraft.tortoise.core.api.Playback;
import ru.krivocraft.tortoise.core.api.settings.ReadOnlySettings;
import ru.krivocraft.tortoise.core.model.LoopType;
import ru.krivocraft.tortoise.core.model.Track;
import ru.krivocraft.tortoise.core.model.TrackList;
import ru.krivocraft.tortoise.core.rating.Shuffle;
import ru.krivocraft.tortoise.android.settings.SettingsStorageManager;
import ru.krivocraft.tortoise.android.tracklist.TracksStorageManager;

import java.util.ArrayList;
import java.util.List;

class PlaybackManager implements AudioFocus.ChangeListener, Playback {

    private final TracksStorageManager tracksStorageManager;

    private int playerState;

    private final PlayerStateCallback playerStateCallback;
    private final PlaylistUpdateCallback playlistUpdateCallback;

    private final MediaPlayer player;
    private final AudioFocus focus;
    private final ReadOnlySettings settings;

    private Track.Reference cache;

    private TrackList playlist;

    private int cursor = 0;

    PlaybackManager(Context context, PlayerStateCallback playerStateCallback, PlaylistUpdateCallback playlistUpdateCallback) {
        this.playerStateCallback = playerStateCallback;
        this.playlistUpdateCallback = playlistUpdateCallback;

        this.player = new AndroidMediaPlayer(this::proceed, this::onPrepared);
        this.playlist = TrackList.EMPTY;

        this.playerState = PlaybackStateCompat.STATE_NONE;
        this.tracksStorageManager = new TracksStorageManager(context);

        this.focus = new AndroidAudioFocus(this, (AudioManager) context.getSystemService(Context.AUDIO_SERVICE));
        this.settings = new SharedPreferencesSettings(context);

        updatePlaybackState();
        restoreAll();
    }

    private boolean isPlaying() {
        return player.playing();
    }

    public void play() {
        if (cursor >= 0 && tracks().size() > 0) {
            Track.Reference selectedReference = tracks().get(cursor);
            boolean mediaChanged = (cache == null || !cache.equals(selectedReference));
            Track selectedTrack = tracksStorageManager.getTrack(selectedReference);

            focus.request();

            if (mediaChanged) {
                start();
                return;
            }

            player.play();

            selectedTrack.setPlaying(true);
            tracksStorageManager.updateTrack(selectedTrack);

            playerState = PlaybackStateCompat.STATE_PLAYING;
            updatePlaybackState();
        }
    }

    @Override
    public void start() {
        Track track = tracksStorageManager.getTrack(tracks().get(cursor));
        player.reset();
        player.set(track.path());
        player.prepare();
        cache = tracks().get(cursor);
    }

    public void pause() {
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

    public void skipTo(int index) {
        int cursor = index;
        cursor = replaceCursorIfOutOfBounds(index, cursor);

        if (!cursorOutOfBounds(cursor)) {
            pause();
            deselectCurrentTrack();
            this.cursor = cursor;
            if (!settings.read(SettingsStorageManager.KEY_SHOW_IGNORED, false) && tracksStorageManager.getTrack(getSelectedTrackReference()).isIgnored()) {
                next();
                return;
            }
            selectCurrentTrack();
            playerStateCallback.onTrackChanged(tracksStorageManager.getTrack(getSelectedTrackReference()));
            play();
        }
    }

    private int replaceCursorIfOutOfBounds(int index, int cursor) {
        if (index < 0) return playlist.size() - 1;
        if (index >= tracks().size()) return 0;
        return cursor;
    }

    private void selectCurrentTrack() {
        Track.Reference selectedReference = tracks().get(this.cursor);
        Track selectedTrack = tracksStorageManager.getTrack(selectedReference);
        selectedTrack.setSelected(true);
        tracksStorageManager.updateTrack(selectedTrack);
    }

    private boolean cursorOutOfBounds(int cursor) {
        return cursor < 0 || cursor >= tracks().size();
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
        cursor = playlist.shuffle(new Shuffle(tracksStorageManager, settings), getSelectedTrackReference());
        playlistUpdateCallback.onPlaylistUpdated(playlist);
    }

    int cursor() {
        return cursor;
    }

    void proceed() {
        LoopType type = new LoopType.Of(settings.read("loop_type", TrackList.LOOP_TRACK_LIST), this).get();
        type.action().execute();
    }

    public void next() {
        skipTo(cursor + 1);
    }

    public void previous() {
        skipTo(cursor - 1);
    }

    public void stop() {
        if (player != null) {
            focus.release();

            deselectCurrentTrack();
            cache = null;

            player.stop();
            playerState = PlaybackStateCompat.STATE_STOPPED;
            updatePlaybackState();
        }
    }

    public void seekTo(int position) {
        pause();
        player.seekTo(position);
        play();
    }

    public Track.Reference current() {
        return tracks().get(cursor());
    }

    void setTrackList(TrackList trackList, boolean sendUpdate) {
        if (trackList != this.playlist) {
            this.playlist = trackList;
            if (sendUpdate && playlistUpdateCallback != null) {
                playlistUpdateCallback.onPlaylistUpdated(trackList);
            }
        }
    }

    void setCursor(int cursor) {
        this.cursor = cursor;
    }

    public List<Track.Reference> tracks() {
        if (getPlaylist() != null)
            return getPlaylist().getTrackReferences();
        else
            return new ArrayList<>();
    }

    TrackList getPlaylist() {
        return playlist;
    }

    int getCurrentStreamPosition() {
        return player.position();
    }

    Track.Reference getSelectedTrackReference() {
        if (tracks() != null && tracks().size() > 0) {
            return tracks().get(cursor);
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

    private void setVolume(float volume) {
        player.volume(volume);
    }

    void destroy() {
        focus.release();
        player.release();
    }

    public void onPrepared() {
        play();
        Track selectedTrack = tracksStorageManager.getTrack(tracks().get(cursor));
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
