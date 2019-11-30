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

package ru.krivocraft.kbmp.core;

import android.content.Context;
import android.database.sqlite.SQLiteException;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ru.krivocraft.kbmp.core.storage.TrackListsStorageManager;
import ru.krivocraft.kbmp.core.storage.TracksStorageManager;
import ru.krivocraft.kbmp.core.track.Track;
import ru.krivocraft.kbmp.core.track.TrackList;
import ru.krivocraft.kbmp.tasks.compilers.CompileByAuthorTask;
import ru.krivocraft.kbmp.tasks.compilers.CompileFavoritesTask;
import ru.krivocraft.kbmp.tasks.compilers.CompileTrackListsTask;

public class TrackListsCompiler {
    private final TracksStorageManager tracksStorageManager;
    private final TrackListsStorageManager trackListsStorageManager;

    public TrackListsCompiler(Context context) {
        this.tracksStorageManager = new TracksStorageManager(context);
        this.trackListsStorageManager = new TrackListsStorageManager(context, TrackListsStorageManager.FILTER_ALL);
    }

    public void compileByAuthors(OnTrackListsCompiledCallback callback) {
        CompileByAuthorTask task = new CompileByAuthorTask();
        compile(task, TrackList.TRACK_LIST_BY_AUTHOR, callback);
    }

    public void compileFavorites(OnTrackListsCompiledCallback callback) {
        try {
            trackListsStorageManager.clearTrackList(TrackList.createIdentifier(TrackListsStorageManager.FAVORITES_DISPLAY_NAME));
        } catch (SQLiteException e) {
            //No trackList available
        }
        CompileFavoritesTask task = new CompileFavoritesTask();
        compile(task, TrackList.TRACK_LIST_CUSTOM, callback);
    }

    private void compile(CompileTrackListsTask task, int trackListType, OnTrackListsCompiledCallback callback) {
        List<TrackList> list = new LinkedList<>();
        task.setListener(trackLists -> new Thread(() -> parseMap(callback, list, trackLists, trackListType)).start());
        task.execute(tracksStorageManager.getTrackStorage().toArray(new Track[0]));
    }

    private void parseMap(OnTrackListsCompiledCallback callback, List<TrackList> list, Map<String, List<Track>> trackLists, int trackListByTag) {
        for (Map.Entry<String, List<Track>> entry : trackLists.entrySet()) {
            TrackList trackList = new TrackList(entry.getKey(), tracksStorageManager.getReferences(entry.getValue()), trackListByTag);
            list.add(trackList);
        }
        callback.onTrackListsCompiled(list);
    }

}
