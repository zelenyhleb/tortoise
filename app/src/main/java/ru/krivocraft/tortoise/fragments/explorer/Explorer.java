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

package ru.krivocraft.tortoise.fragments.explorer;

import android.content.Context;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import ru.krivocraft.tortoise.sorting.TrackListsCompiler;
import ru.krivocraft.tortoise.core.storage.TrackListsStorageManager;
import ru.krivocraft.tortoise.core.track.TrackList;

import java.util.ArrayList;
import java.util.List;

public class Explorer {

    private final TrackListsStorageManager tracksStorageManager;
    private final TrackListsCompiler trackListsCompiler;
    private final OnTrackListsCompiledListener listener;
    private List<TrackList> customLists = new ArrayList<>();
    private List<TrackList> sortedLists = new ArrayList<>();

    public Explorer(OnTrackListsCompiledListener listener, @NonNull Context context) {
        this.listener = listener;
        this.tracksStorageManager = new TrackListsStorageManager(context, TrackListsStorageManager.FILTER_ALL);
        this.trackListsCompiler = new TrackListsCompiler(context);
        updateTrackListSets(listener);
        System.out.println("created");
    }

    private void requestUpdate() {
        AsyncTask.execute(() -> updateTrackListSets(listener));
    }

    private void updateTrackListSets(OnTrackListsCompiledListener listener) {
        this.customLists = tracksStorageManager.readCustom();
        this.sortedLists = tracksStorageManager.readSortedByArtist();
        listener.onTrackListsCompiled();
    }

    private void onNewTrackLists(List<TrackList> trackLists) {
        for (TrackList trackList : trackLists) {
            if (tracksStorageManager.getExistingTrackListNames().contains(trackList.getDisplayName())) {
                tracksStorageManager.clearTrackList(trackList.getIdentifier());
                tracksStorageManager.updateTrackListContent(trackList);
            } else {
                tracksStorageManager.writeTrackList(trackList);
            }
        }

        for (TrackList trackList : tracksStorageManager.readSortedByArtist()) {
            if (trackList.size() <= 1) {
                tracksStorageManager.removeTrackList(trackList);
            }
        }

        requestUpdate();
    }

    void compileTrackLists() {
        trackListsCompiler.compileFavorites(this::onNewTrackLists);
        trackListsCompiler.compileByAuthors(this::onNewTrackLists);
    }

    List<TrackList> getCustomLists() {
        return customLists;
    }

    List<TrackList> getSortedLists() {
        return sortedLists;
    }

    public interface OnTrackListsCompiledListener {
        void onTrackListsCompiled();
    }

}
