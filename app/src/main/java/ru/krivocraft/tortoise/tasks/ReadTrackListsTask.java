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

package ru.krivocraft.tortoise.tasks;

import android.content.SharedPreferences;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

import ru.krivocraft.tortoise.core.storage.SettingsStorageManager;
import ru.krivocraft.tortoise.core.track.TrackList;
import ru.krivocraft.tortoise.sqlite.DBConnection;

public class ReadTrackListsTask extends AsyncTask<Void, Integer, List<TrackList>> {

    private SharedPreferences settingsStorage;
    private OnTrackListsReadCallback callback;
    private DBConnection connection;

    public ReadTrackListsTask(DBConnection connection, SharedPreferences settingsStorage, OnTrackListsReadCallback callback) {
        this.settingsStorage = settingsStorage;
        this.callback = callback;
        this.connection = connection;
    }

    @Override
    protected List<TrackList> doInBackground(Void... voids) {
        List<TrackList> allTrackLists = new ArrayList<>();
        List<TrackList> storedTrackLists = connection.getTrackLists();
        for (TrackList trackList : storedTrackLists) {
            if (trackList.getType() == TrackList.TRACK_LIST_BY_AUTHOR) {
                if (settingsStorage.getBoolean(SettingsStorageManager.KEY_SORT_BY_ARTIST, false)) {
                    allTrackLists.add(trackList);
                }
            } else if (trackList.getType() == TrackList.TRACK_LIST_BY_TAG) {
                if (settingsStorage.getBoolean(SettingsStorageManager.KEY_SORT_BY_TAG, false)) {
                    allTrackLists.add(trackList);
                }
            } else {
                allTrackLists.add(trackList);
            }
        }
        return allTrackLists;
    }

    @Override
    protected void onPostExecute(List<TrackList> trackLists) {
        super.onPostExecute(trackLists);
        callback.onTrackListsRead(trackLists);
    }
}
