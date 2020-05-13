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

package ru.krivocraft.tortoise.core.seek;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import ru.krivocraft.tortoise.core.model.Track;
import ru.krivocraft.tortoise.sorting.OnStorageUpdateCallback;
import ru.krivocraft.tortoise.thumbnail.Colors;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ContentResolverSeek extends SeekTask<ContentResolver> {

    public ContentResolverSeek(OnStorageUpdateCallback callback, boolean recognize, ContentResolver seekBase) {
        super(callback, recognize, seekBase);
    }

    @Override
    public List<Track> seek(ContentResolver contentResolver) {
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        String[] projection = {
                MediaStore.Audio.Media.DATA,
        };
        final String sortOrder = MediaStore.Audio.AudioColumns.DATE_MODIFIED + " COLLATE LOCALIZED ASC";
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = contentResolver.query(uri, projection, selection, null, sortOrder);
        List<String> paths = new LinkedList<>();
        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                String path = cursor.getString(0);
                cursor.moveToNext();
                if (path != null && path.endsWith(".mp3")) {
                    paths.add(path);
                }
            }
            cursor.close();
        }

        return tracks(paths);
    }

    private List<Track> tracks(List<String> paths) {
        List<Track> tracks = new LinkedList<>();
        for (String path : paths) {
            tracks.add(fromFile(new File(path)));
        }
        return tracks;
    }
}
