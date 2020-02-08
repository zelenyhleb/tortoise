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

package ru.krivocraft.tortoise.core;

import android.content.Context;
import ru.krivocraft.tortoise.core.storage.TrackListsStorageManager;
import ru.krivocraft.tortoise.core.storage.TracksStorageManager;
import ru.krivocraft.tortoise.core.track.Track;
import ru.krivocraft.tortoise.core.track.TrackList;
import ru.krivocraft.tortoise.core.track.TrackReference;

import java.util.ArrayList;
import java.util.List;

public class Searcher {

    private final TracksStorageManager tracksStorageManager;
    private final TrackListsStorageManager trackListsStorageManager;

    public Searcher(Context context) {
        this.tracksStorageManager = new TracksStorageManager(context);
        this.trackListsStorageManager = new TrackListsStorageManager(context, TrackListsStorageManager.FILTER_ALL);
    }

    public List<TrackReference> search(CharSequence string, List<TrackReference> input) {
        List<TrackReference> references = new ArrayList<>();
        List<Track> searched = tracksStorageManager.getTracks(input);
        List<TrackList> trackLists = trackListsStorageManager.readAllTrackLists();
        for (Track track : searched) {

            String formattedName = track.getTitle().toLowerCase();
            String formattedArtist = track.getArtist().toLowerCase();
            String formattedSearchStr = string.toString().toLowerCase();

            if (formattedName.contains(formattedSearchStr) || formattedArtist.contains(formattedSearchStr)) {
                references.add(input.get(searched.indexOf(track)));
            }
        }

        for (TrackList trackList : trackLists) {
            if (trackList.getDisplayName().contains(string)) {
                for (TrackReference reference : trackList.getTrackReferences()) {
                    if (!references.contains(reference)) {
                        references.add(reference);
                    }
                }
            }
        }
        return references;
    }

    public List<Track> searchInTracks(CharSequence string, List<Track> input) {
        List<Track> found = new ArrayList<>();
        List<TrackList> trackLists = trackListsStorageManager.readAllTrackLists();
        for (Track track : input) {

            String formattedName = track.getTitle().toLowerCase();
            String formattedArtist = track.getArtist().toLowerCase();
            String formattedSearchStr = string.toString().toLowerCase();
            if (formattedName.contains(formattedSearchStr) || formattedArtist.contains(formattedSearchStr)) {
                found.add(input.get(input.indexOf(track)));
            }
        }

        for (TrackList trackList : trackLists) {
            if (trackList.getDisplayName().contains(string)) {
                for (TrackReference trackReference : trackList.getTrackReferences()) {
                    Track track = tracksStorageManager.getTrack(trackReference);
                    if (!found.contains(track)) {
                        found.add(track);
                    }
                }
            }
        }
        return found;
    }
}
