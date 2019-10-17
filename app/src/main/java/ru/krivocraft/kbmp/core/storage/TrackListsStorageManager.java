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

package ru.krivocraft.kbmp.core.storage;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.List;

import ru.krivocraft.kbmp.core.track.TrackList;
import ru.krivocraft.kbmp.core.track.TrackReference;
import ru.krivocraft.kbmp.sqlite.DBConnection;

public class TrackListsStorageManager {

    public static final String STORAGE_TRACKS_DISPLAY_NAME = "All tracks";
    public static final String FAVORITES_DISPLAY_NAME = "Favorites";

    private final DBConnection database;

    public TrackListsStorageManager(@NonNull Context context) {
        this.database = new DBConnection(context);
    }

    public void updateTrackListData(TrackList trackList) {
        database.updateTrackListData(trackList);
    }

    public void updateTrackListContent(TrackList trackList) {
        database.updateTrackListContent(trackList);
    }

    public void clearTrackList(String trackList) {
        database.clearTrackList(trackList);
    }

    public void updateRootTrackList(TrackList trackList) {
        database.updateRootTrackList(trackList);
    }

    public void removeTracks(TrackList trackList, List<TrackReference> references) {
        database.removeTracks(trackList, references);
    }

    public void writeTrackList(TrackList trackList) {
        database.writeTrackList(trackList);
    }

    public void removeTrackList(TrackList trackList) {
        database.removeTrackList(trackList);
    }

    public List<TrackList> readAllTrackLists() {
        return database.getTrackLists();
    }

    public List<TrackList> readTrackLists(boolean sortByTag, boolean sortByAuthor) {
        return database.getTrackLists(sortByTag, sortByAuthor);
    }

    public List<String> getExistingTrackListNames() {
        return database.getTrackListNames();
    }

}
