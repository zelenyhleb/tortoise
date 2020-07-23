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

package ru.krivocraft.tortoise.core.explorer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import ru.krivocraft.tortoise.core.model.TrackList;
import ru.krivocraft.tortoise.core.tracklist.TracksProvider;
import ru.krivocraft.tortoise.core.sorting.TrackListsCompiler;
import ru.krivocraft.tortoise.core.tracklist.TracksStorageManager;

import java.util.ArrayList;
import java.util.List;

public class Explorer {

    private final TrackListsStorageManager tracksStorageManager;
    private final TrackListsCompiler trackListsCompiler;
    private final OnTrackListsCompiledListener listener;
    private List<TrackList> customLists = new ArrayList<>();
    private List<TrackList> sortedLists = new ArrayList<>();

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            requestUpdate();
        }
    };

    public Explorer(OnTrackListsCompiledListener listener, @NonNull Context context) {
        this.listener = listener;
        this.tracksStorageManager = new TrackListsStorageManager(context, TrackListsStorageManager.FILTER_ALL);
        this.trackListsCompiler = new TrackListsCompiler(new TracksStorageManager(context));
        updateTrackListSets(listener);
        context.registerReceiver(receiver, new IntentFilter(TracksProvider.ACTION_UPDATE_STORAGE));
    }

    private void requestUpdate() {
        AsyncTask.execute(this::compileTrackLists);
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
        updateTrackListSets(listener);
    }

    public void unregisterReceiver(Context context) {
        context.unregisterReceiver(receiver);
    }

    private void compileTrackLists() {
        trackListsCompiler.compileAll(this::onNewTrackLists);
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
