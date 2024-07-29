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
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import ru.krivocraft.tortoise.android.AndroidAudioFocus;
import ru.krivocraft.tortoise.android.AndroidMediaPlayer;
import ru.krivocraft.tortoise.android.PlaybackState;
import ru.krivocraft.tortoise.android.settings.SettingsStorageManager;
import ru.krivocraft.tortoise.android.tracklist.TracksStorageManager;
import ru.krivocraft.tortoise.core.api.AudioFocus;
import ru.krivocraft.tortoise.core.api.MediaPlayer;
import ru.krivocraft.tortoise.core.api.Playback;
import ru.krivocraft.tortoise.core.api.Stamp;
import ru.krivocraft.tortoise.core.api.settings.WriteableSettings;
import ru.krivocraft.tortoise.core.model.LoopType;
import ru.krivocraft.tortoise.core.model.Track;
import ru.krivocraft.tortoise.core.model.TrackList;
import ru.krivocraft.tortoise.core.rating.Shuffle;

class PlaybackManager implements AudioFocus.ChangeListener, Playback {

    private final TracksStorageManager tracksStorageManager;

    private final PlayerStateCallback playerStateCallback;
    private final PlaylistUpdateCallback playlistUpdateCallback;

    private final MediaPlayer player;
    private final AudioFocus focus;
    private final WriteableSettings settings;

    private TrackList playlist;
    private Track.Reference cache;

    private int cursor = 0;

    PlaybackManager(Context context, PlayerStateCallback playerStateCallback, PlaylistUpdateCallback playlistUpdateCallback) {
        this.playerStateCallback = playerStateCallback;
        this.playlistUpdateCallback = playlistUpdateCallback;

        this.player = new AndroidMediaPlayer(this::proceed);
        this.playlist = TrackList.EMPTY;

        this.tracksStorageManager = new TracksStorageManager(context);

        this.focus = new AndroidAudioFocus(this, (AudioManager) context.getSystemService(Context.AUDIO_SERVICE));
        this.settings = new SharedPreferencesSettings(context);

        updatePlaybackState(PlaybackStateCompat.STATE_NONE);
        restoreAll();
    }

    private boolean playing() {
        return player.playing();
    }

    public void play() {
        if (cursor >= 0 && !tracks().isEmpty()) {
            boolean mediaChanged = (cache == null || !cache.equals(current()));
            Track selectedTrack = tracksStorageManager.getTrack(current());

            focus.request();

            if (mediaChanged) {
                start();
            }

            player.play();

            selectedTrack.setPlaying(true);
            tracksStorageManager.updateTrack(selectedTrack);

            updatePlaybackState(PlaybackStateCompat.STATE_PLAYING);
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
        if (playing()) {
            player.pause();
        }

        if (previousTrackExists()) {
            Track selectedTrack = tracksStorageManager.getTrack(cache);
            selectedTrack.setPlaying(false);
            tracksStorageManager.updateTrack(selectedTrack);
        }
        focus.release();
        updatePlaybackState(PlaybackStateCompat.STATE_PAUSED);
        new ActualStamp(settings, e -> Log.e("Err", e)).persist(currentStamp());
    }

    private @NonNull Stamp currentStamp() {
        return new Stamp(tracks(), cursor, player.position());
    }

    public void skipTo(int index) {
        int cursor = index;
        cursor = replaceCursorIfOutOfBounds(index, cursor);
        if (cursorOutOfBounds(cursor)) {
            return;
        }
        pause();
        deselectCurrentTrack();
        this.cursor = cursor;
        if (!settings.read(SettingsStorageManager.KEY_SHOW_IGNORED, false) && tracksStorageManager.getTrack(selected()).isIgnored()) {
            next();
            return;
        }
        selectCurrentTrack();
        playerStateCallback.onTrackChanged(tracksStorageManager.getTrack(selected()));
        play();
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
        cursor = playlist.shuffle(new Shuffle(tracksStorageManager, settings), selected());
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
            updatePlaybackState(PlaybackStateCompat.STATE_STOPPED);
        }
    }

    public void seekTo(int position) {
        pause();
        player.seekTo(position);
        play();
    }

    @Override
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
        if (playlist() != null)
            return playlist().getTrackReferences();
        else
            return new ArrayList<>();
    }

    TrackList playlist() {
        return playlist;
    }

    int position() {
        return player.position();
    }

    Track.Reference selected() {
        if (tracks() != null && !tracks().isEmpty()) {
            return tracks().get(cursor);
        }
        return null;
    }

    private void updatePlaybackState(int playerState) {
        playerStateCallback.onPlaybackStateChanged(new PlaybackState().apply(playerState, position()));
    }

    private void setVolume(float volume) {
        player.volume(volume);
    }

    void destroy() {
        focus.release();
        player.release();
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
