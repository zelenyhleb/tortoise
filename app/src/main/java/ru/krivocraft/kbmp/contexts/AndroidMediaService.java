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

package ru.krivocraft.kbmp.contexts;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media.MediaBrowserServiceCompat;

import java.util.List;

import ru.krivocraft.kbmp.core.playback.MediaService;

public class AndroidMediaService extends MediaBrowserServiceCompat {

    public static boolean running = false;

    private MediaService mediaService;

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return new BrowserRoot("", null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        //Do nothing yet.
    }

    @Override
    public void onDestroy() {
        this.mediaService.destroy();
        running = false;
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.mediaService = new MediaService(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.mediaService.handleCommand(intent);
        running = true;
        return START_NOT_STICKY;
    }

}
