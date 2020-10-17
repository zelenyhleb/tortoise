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

package ru.krivocraft.tortoise.core.search;

import ru.krivocraft.tortoise.core.data.TrackListsProvider;
import ru.krivocraft.tortoise.core.data.TracksProvider;
import ru.krivocraft.tortoise.core.model.Track;

import java.util.ArrayList;
import java.util.List;

public final class Search {

    private final TracksProvider tracksProvider;
    private final TrackListsProvider trackListsProvider;

    public Search(TracksProvider tracksProvider, TrackListsProvider trackListsProvider) {
        this.tracksProvider = tracksProvider;
        this.trackListsProvider = trackListsProvider;
    }

    public List<Track.Reference> search(CharSequence query, List<Track.Reference> input) {
        List<Track.Reference> references = new ArrayList<>();
        findTracks(query, references, input);
        findPlaylists(query, references);
        return references;
    }

    private void findTracks(CharSequence query, List<Track.Reference> references, List<Track.Reference> input) {
        List<Track> scope = tracksProvider.getTracks(input);
        scope.stream()
                .map(new TrackEntry())
                .filter(new Query(query.toString()))
                .map(Entry::track)
                .map(track -> input.get(scope.indexOf(track)))
                .forEach(new AddIfNotAdded(references));
    }

    private void findPlaylists(CharSequence string, List<Track.Reference> references) {
        trackListsProvider.readAllTrackLists().stream()
                .filter(list -> list.getDisplayName().contains(string))
                .flatMap(trackList -> trackList.getTrackReferences().stream())
                .forEach(new AddIfNotAdded(references));
    }

}
