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

package ru.krivocraft.tortoise.core.rating;

import ru.krivocraft.tortoise.core.api.settings.ReadOnlySettings;
import ru.krivocraft.tortoise.core.model.Track;
import ru.krivocraft.tortoise.core.model.TrackList;
import ru.krivocraft.tortoise.core.data.TracksProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Shuffle {

    private final TracksProvider tracksStorageManager;
    private final ReadOnlySettings settings;

    public static final String KEY_SMART_SHUFFLE = "smartShuffle";

    public Shuffle(TracksProvider tracksStorageManager, ReadOnlySettings settings) {
        this.tracksStorageManager = tracksStorageManager;
        this.settings = settings;
    }

    public List<Track.Reference> shuffle(TrackList trackList, Track.Reference firstTrack) {
        trackList.getTrackReferences().remove(firstTrack);
        List<Track.Reference> references =
                settings.read(KEY_SMART_SHUFFLE, true) ?
                        smartShuffle(trackList) : basicShuffle(trackList);
        references.add(0, firstTrack);
        return references;
    }

    private List<Track.Reference> basicShuffle(TrackList trackList) {
        Collections.shuffle(trackList.getTrackReferences());
        return trackList.getTrackReferences();
    }

    private List<Track.Reference> smartShuffle(List<Track.Reference> references) {
        List<Track> tracks = tracksStorageManager.getTracks(references);
        Random random = new Random(System.currentTimeMillis());
        List<Track> shuffled = new ArrayList<>();

        List<Integer> pool = new ArrayList<>();
        int min = Math.abs(Collections.min(ratings(tracks))) + 1;
        for (Track element : tracks) {
            for (int i = 0; i < element.getRating() + min; i++) {
                pool.add(tracks.indexOf(element));
            }
        }

        while (pool.size() > 0) {
            int index = pool.get(random.nextInt(pool.size()));
            shuffled.add(tracks.get(index));
            pool.removeAll(Collections.singleton(index));
        }
        return tracksStorageManager.getReferences(shuffled);
    }

    private List<Integer> ratings(List<Track> tracks) {
        List<Integer> ratings = new ArrayList<>();
        for (Track track : tracks) {
            ratings.add(track.getRating());
        }
        return ratings;
    }

    private List<Track.Reference> smartShuffle(TrackList trackList) {
        return smartShuffle(trackList.getTrackReferences());
    }

}
