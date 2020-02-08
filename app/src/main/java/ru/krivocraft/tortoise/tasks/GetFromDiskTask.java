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

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import ru.krivocraft.tortoise.core.ColorManager;
import ru.krivocraft.tortoise.core.track.Track;

import java.util.ArrayList;
import java.util.List;

public class GetFromDiskTask extends AsyncTask<Void, Integer, List<Track>> {

    private ContentResolver contentResolver;
    private OnStorageUpdateCallback callback;
    private ColorManager colorManager;
    private boolean recognize;

    public GetFromDiskTask(ContentResolver contentResolver, boolean recognize, OnStorageUpdateCallback callback, ColorManager colorManager) {
        this.contentResolver = contentResolver;
        this.recognize = recognize;
        this.callback = callback;
        this.colorManager = colorManager;
    }

    @Override
    protected List<Track> doInBackground(Void... voids) {
        return search(contentResolver, recognize);
    }

    @Override
    protected void onPostExecute(List<Track> tracks) {
        super.onPostExecute(tracks);
        callback.onStorageUpdate(tracks);
    }

    private List<Track> search(ContentResolver contentResolver, boolean recognize) {
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        String[] projection = {
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION
        };
        final String sortOrder = MediaStore.Audio.AudioColumns.DATA + " COLLATE LOCALIZED ASC";
        List<Track> storage = new ArrayList<>();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = contentResolver.query(uri, projection, selection, null, sortOrder);
        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {

                String path = cursor.getString(0);
                String artist = cursor.getString(1);
                String title = cursor.getString(2);
                long duration = cursor.getLong(3);

                if (recognize) {
                    String[] meta = title.split(" - ");
                    if (meta.length > 1) {
                        artist = meta[0];
                        title = meta[1];
                    } else {
                        meta = title.split(" — ");
                        if (meta.length > 1) {
                            artist = meta[0];
                            title = meta[1];
                        }
                    }
                }

                cursor.moveToNext();
                if (path != null && path.endsWith(".mp3") && duration > 0) {
                    storage.add(new Track(duration, artist, title, path, colorManager.getRandomColor()));
                }
            }
            cursor.close();
        }
        return storage;
    }
}
