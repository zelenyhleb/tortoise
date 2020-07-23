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

package ru.krivocraft.tortoise.core.sorting;

import ru.krivocraft.tortoise.core.data.TracksProvider;
import ru.krivocraft.tortoise.core.model.Track;
import ru.krivocraft.tortoise.core.model.TrackList;
import ru.krivocraft.tortoise.core.sorting.compilers.CompileByAuthorTask;
import ru.krivocraft.tortoise.core.sorting.compilers.CompileFavoritesTask;
import ru.krivocraft.tortoise.core.sorting.compilers.CompileTrackListsTask;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TrackListsCompiler {
    private final TracksProvider tracksProvider;

    public TrackListsCompiler(TracksProvider tracksProvider) {
        this.tracksProvider = tracksProvider;
    }

    public void compileAll(OnTrackListsCompiledCallback callback) {
        compileFavorites(callback);
        compileByAuthors(callback);
    }

    private void compileByAuthors(OnTrackListsCompiledCallback callback) {
        CompileByAuthorTask task = new CompileByAuthorTask();
        compile(task, TrackList.TRACK_LIST_BY_AUTHOR, callback);
    }

    private void compileFavorites(OnTrackListsCompiledCallback callback) {
        CompileFavoritesTask task = new CompileFavoritesTask();
        compile(task, TrackList.TRACK_LIST_CUSTOM, callback);
    }

    private void compile(CompileTrackListsTask task, int trackListType, OnTrackListsCompiledCallback callback) {
        task.setListener(trackLists -> parseMap(callback, trackLists, trackListType));
        task.execute(tracksProvider.getTrackStorage().toArray(new Track[0]));
    }

    private void parseMap(OnTrackListsCompiledCallback callback, Map<String, List<Track>> trackLists, int trackListByTag) {
        List<TrackList> list = new LinkedList<>();
        for (Map.Entry<String, List<Track>> entry : trackLists.entrySet()) {
            TrackList trackList = new TrackList(entry.getKey(), tracksProvider.getReferences(entry.getValue()), trackListByTag);
            list.add(trackList);
        }
        callback.onTrackListsCompiled(list);
    }

}
