/*
 * Copyright (c) 2020 Nikifor Fedorov
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
 *         Nikifor Fedorov and others
 */

package ru.krivocraft.tortoise.android.seek;

import ru.krivocraft.tortoise.core.model.Track;
import ru.krivocraft.tortoise.core.sorting.OnStorageUpdateCallback;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class FileSystemSeek extends SeekTask<File> {

    public FileSystemSeek(OnStorageUpdateCallback callback, boolean recognize, File seekBase) {
        super(callback, recognize, seekBase);
    }

    @Override
    public List<Track> seek(File directory) {
        List<Track> tracks = new LinkedList<>();
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    tracks.addAll(seek(file));
                } else {
                    if (file.getPath().endsWith(".mp3")) {
                        tracks.add(retrieveTrack.from(file));
                    }
                }
            }
        } else {
            System.out.println("search failed");
        }
        return tracks;
    }
}
