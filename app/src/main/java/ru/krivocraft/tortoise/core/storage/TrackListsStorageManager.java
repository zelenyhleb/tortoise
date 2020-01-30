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

package ru.krivocraft.tortoise.core.storage;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.List;

import ru.krivocraft.tortoise.core.track.TrackList;
import ru.krivocraft.tortoise.core.track.TrackReference;
import ru.krivocraft.tortoise.sqlite.DBConnection;

public class TrackListsStorageManager {

    public static final String STORAGE_TRACKS_DISPLAY_NAME = "All tracks";
    public static final String FAVORITES_DISPLAY_NAME = "Favorites";

    private final DBConnection database;
    private final String filter;

    public final static String FILTER_CUSTOM = "custom";
    public final static String FILTER_ARTIST = "artist";
    public final static String FILTER_ALL = "all";

    public TrackListsStorageManager(@NonNull Context context, String filter) {
        this.database = new DBConnection(context);
        this.filter = filter;
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
        switch (filter) {
            case FILTER_CUSTOM:
                return readCustom();
            case FILTER_ARTIST:
                return readSortedByArtist();
            default:
                return database.getTrackLists();
        }
    }

    public List<TrackList> readSortedByArtist() {
        return database.getSortedByArtist();
    }

    public List<TrackList> readCustom() {
        return database.getCustom();
    }

    public List<String> getExistingTrackListNames() {
        return database.getTrackListNames();
    }

}
