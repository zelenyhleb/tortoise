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

package ru.krivocraft.tortoise.core.sorting.compilers;

import ru.krivocraft.tortoise.core.model.Track;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompileByAuthorTask extends CompileTrackListsTask {

    @Override
    protected Map<String, List<Track>> doInBackground(Track... source) {
        Map<String, List<Track>> playlistMap = new HashMap<>();
        for (Track track : source) {
            String artist = track.getArtist();
            List<Track> trackList = playlistMap.get(artist);
            if (trackList == null) {
                trackList = new ArrayList<>();
                playlistMap.put(artist, trackList);
            }
            if (!trackList.contains(track)) {
                trackList.add(track);
            }
        }
        String SINGLE_DISPLAY_NAME = "Single Tracks";
        List<Track> singleTracks = new ArrayList<>();
        List<String> tracksToRemove = new ArrayList<>();
        for (Map.Entry<String, List<Track>> entry : playlistMap.entrySet()) {
            List<Track> value = entry.getValue();
            if (value.size() <= 1) {
                //Get the only track contained in this list
                singleTracks.add(value.get(0));
                tracksToRemove.add(entry.getKey());
            }
        }

        for (String key : tracksToRemove) {
            playlistMap.remove(key);
        }

        playlistMap.put(SINGLE_DISPLAY_NAME, singleTracks);
        return playlistMap;
    }

}
