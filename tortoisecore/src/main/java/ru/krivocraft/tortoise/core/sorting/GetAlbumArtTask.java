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

package ru.krivocraft.tortoise.core.sorting;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import ru.krivocraft.tortoise.core.model.Track;
import ru.krivocraft.tortoise.core.utils.Art;

public class GetAlbumArtTask extends AsyncTask<Track, Integer, Bitmap> {

    private final OnAlbumArtAcquiredCallback callback;

    public GetAlbumArtTask(OnAlbumArtAcquiredCallback callback) {
        this.callback = callback;
    }

    @Override
    protected Bitmap doInBackground(Track... tracks) {
        for (Track track : tracks) {
            Bitmap bitmap = new Art(track.getPath()).bitmap();
            if (bitmap != null) {
                return bitmap;
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        callback.onAlbumArtAcquired(bitmap);
    }
}
