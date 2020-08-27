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

package ru.krivocraft.tortoise.core.tracklist;

import android.content.Context;
import org.apache.commons.collections4.CollectionUtils;
import ru.krivocraft.tortoise.core.PreferencesManager;
import ru.krivocraft.tortoise.core.api.settings.ReadOnlySettings;
import ru.krivocraft.tortoise.core.player.SharedPreferencesSettings;
import ru.krivocraft.tortoise.core.data.TracksProvider;
import ru.krivocraft.tortoise.core.model.Track;
import ru.krivocraft.tortoise.core.model.TrackReference;
import ru.krivocraft.tortoise.sqlite.DBConnection;

import java.util.ArrayList;
import java.util.List;

public class TracksStorageManager implements TracksProvider {

    private final DBConnection database;

    public TracksStorageManager(Context context) {
        ReadOnlySettings settings = new SharedPreferencesSettings(
                context.getSharedPreferences(PreferencesManager.STORAGE_SETTINGS, Context.MODE_PRIVATE));
        this.database = new DBConnection(context, settings);
    }

    public List<Track> getTrackStorage() {
        return database.getTracksStorage();
    }

    public void updateTrackStorage(List<Track> tracks) {
        for (Track track : tracks) {
            updateTrack(track);
        }
    }

    public List<Track> getTracks(List<TrackReference> references) {
        List<Track> tracks = new ArrayList<>();
        for (TrackReference reference : references) {
            tracks.add(getTrack(reference));
        }
        return tracks;
    }

    public void updateTrack(Track updatedTrack) {
        database.updateTrack(updatedTrack);
    }

    public void writeTrack(Track track) {
        database.writeTrack(track);
    }

    public void removeTrack(Track track) {
        database.removeTrack(track);
    }

    public TrackReference getReference(String path) {
        List<Track> trackStorage = getTrackStorage();
        List<String> paths = new ArrayList<>(CollectionUtils.collect(trackStorage, Track::getPath));
        return new TrackReference(trackStorage.get(paths.indexOf(path)));
    }

    public List<TrackReference> getReferences(List<Track> tracks) {
        List<TrackReference> references = new ArrayList<>();
        for (Track track : tracks) {
            references.add(getReference(track.getPath()));
        }
        return references;
    }

    public Track getTrack(TrackReference reference) {
        return database.getTrack(reference);
    }
}
