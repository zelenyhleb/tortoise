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

package ru.krivocraft.tortoise.android.tracklist;

import android.content.Context;
import android.content.Intent;

import ru.krivocraft.tortoise.android.explorer.TrackListsStorageManager;
import ru.krivocraft.tortoise.android.player.SharedPreferencesSettings;
import ru.krivocraft.tortoise.core.model.Track;
import ru.krivocraft.tortoise.core.model.TrackList;
import ru.krivocraft.tortoise.android.seek.ContentResolverSeek;
import ru.krivocraft.tortoise.android.settings.SettingsStorageManager;

import java.util.ArrayList;
import java.util.List;

public class TracksProvider {

    public static final String ACTION_UPDATE_STORAGE = "action_update_storage";

    private final Context context;

    private final TrackListsStorageManager trackListsStorageManager;
    private final TracksStorageManager tracksStorageManager;
    private final SettingsStorageManager settings;

    public TracksProvider(Context context) {
        this.trackListsStorageManager = new TrackListsStorageManager(context, TrackListsStorageManager.FILTER_ALL);
        this.tracksStorageManager = new TracksStorageManager(context);
        this.settings = new SettingsStorageManager(new SharedPreferencesSettings(context));
        this.context = context;
    }

    public void search() {
        boolean recognize = settings.get(SettingsStorageManager.KEY_RECOGNIZE_NAMES, true);
        new ContentResolverSeek(this::manageStorage, recognize, context.getContentResolver()).execute();
    }

    private void manageStorage(List<Track> tracks) {
        List<Track.Reference> allTracks = new ArrayList<>();

        removeNonExistingTracksFromStorage(tracksStorageManager.getTrackStorage(), tracks);
        addNewTracks(allTracks, tracksStorageManager.getTrackStorage(), tracks);

        writeRootTrackList(allTracks);

        notifyTracksStorageChanged();
    }

    private void notifyTracksStorageChanged() {
        context.sendBroadcast(new Intent(ACTION_UPDATE_STORAGE));
    }

    private void writeRootTrackList(List<Track.Reference> allTracks) {
        TrackList trackList = new TrackList(TrackList.STORAGE_TRACKS_DISPLAY_NAME, allTracks, TrackList.TRACK_LIST_CUSTOM);
        trackListsStorageManager.updateRootTrackList(trackList);
    }

    private void addNewTracks(List<Track.Reference> allTracks, List<Track> existingTracks, List<Track> readTracks) {
        for (Track track : readTracks) {
            Track.Reference reference = new Track.Reference(track);

            if (!existingTracks.contains(track)) {
                tracksStorageManager.writeTrack(track);
                allTracks.add(reference);
            }
        }
    }

    private void removeNonExistingTracksFromStorage(List<Track> existingTracks, List<Track> readTracks) {
        List<Track.Reference> removedReferences = new ArrayList<>();
        for (Track track : existingTracks) {
            Track.Reference reference = new Track.Reference(track);

            if (!readTracks.contains(track)) {
                tracksStorageManager.removeTrack(track);
                removedReferences.add(reference);
            }
        }
        updateTrackLists(removedReferences);
    }

    private void updateTrackLists(List<Track.Reference> removedTracks) {
        List<TrackList> trackLists = trackListsStorageManager.readAllTrackLists();
        for (TrackList trackList : trackLists) {
            removeNonExistingTracksFromTrackList(removedTracks, trackList);
            removeTrackListIfEmpty(trackList);
        }
    }

    private void removeTrackListIfEmpty(TrackList trackList) {
        if (trackList.size() == 0 && !trackList.getDisplayName().equals(TrackList.STORAGE_TRACKS_DISPLAY_NAME)) {
            trackListsStorageManager.removeTrackList(trackList);
        }
    }

    private void removeNonExistingTracksFromTrackList(List<Track.Reference> removedTracks, TrackList trackList) {
        List<Track.Reference> referencesToRemove = new ArrayList<>();
        for (Track.Reference reference : trackList.getTrackReferences()) {
            if (removedTracks.contains(reference)) {
                referencesToRemove.add(reference);
            }
        }
        trackList.removeAll(referencesToRemove);
        trackListsStorageManager.removeTracks(trackList, referencesToRemove);
    }

}


