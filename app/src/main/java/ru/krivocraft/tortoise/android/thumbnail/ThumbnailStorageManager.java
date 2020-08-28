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

package ru.krivocraft.tortoise.android.thumbnail;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ThumbnailStorageManager {

    private final String rootDir;
    private final String dataDir;

    public ThumbnailStorageManager() {
        this.rootDir = Environment.getExternalStorageDirectory().getAbsolutePath();
        this.dataDir = "/tortoiseThumbnails/";
        File dir = new File(rootDir + dataDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public Bitmap readThumbnail(String identifier) {
        File file = new File(rootDir + dataDir + identifier);
        if (file.exists()) {
            return BitmapFactory.decodeFile(file.getAbsolutePath());
        }
        return null;
    }

    public void writeThumbnail(String path, Bitmap thumbnail) throws IOException {
        File file = new File(rootDir + dataDir + path);
        FileOutputStream stream = new FileOutputStream(file);
        Bitmap.createScaledBitmap(thumbnail, 256, 256, false).compress(Bitmap.CompressFormat.PNG, 100, stream);
        stream.flush();
        stream.close();
    }

    public void removeThumbnail(String identifier) {
        File file = new File(rootDir + dataDir + identifier);
        if (file.exists()) {
            file.delete();
        }
    }

    public void replaceThumbnail(String oldIdentifier, String newIdentifier) throws IOException {
        Bitmap thumbnail = readThumbnail(oldIdentifier);
        writeThumbnail(newIdentifier, thumbnail);
        removeThumbnail(oldIdentifier);
    }

}
