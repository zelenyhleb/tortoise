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

package ru.krivocraft.tortoise.tasks.compilers;

import ru.krivocraft.tortoise.core.storage.TrackListsStorageManager;
import ru.krivocraft.tortoise.core.track.Track;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompileFavoritesTask extends CompileTrackListsTask {

    @Override
    protected Map<String, List<Track>> doInBackground(Track... source) {
        Map<String, List<Track>> trackLists = new HashMap<>();
        List<Track> favorites = new ArrayList<>();
        for (Track track : source) {
            if (track.isLiked()) {
                favorites.add(track);
            }
        }
        if (favorites.size() > 0) {
            trackLists.put(TrackListsStorageManager.FAVORITES_DISPLAY_NAME, favorites);
        }
        return trackLists;
    }
}
