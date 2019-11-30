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

import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.widget.ListView;

import java.util.Arrays;
import java.util.List;

import ru.krivocraft.kbmp.R;
import ru.krivocraft.kbmp.core.settings.SettingsAdapter;
import ru.krivocraft.kbmp.core.storage.SettingsStorageManager;

public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ListView listView = findViewById(R.id.settings_list);
        List<String> settings = getSettings();
        listView.setAdapter(new SettingsAdapter(this, settings));
    }

    @Override
    void init() {
        //Do nothing
    }

    @Override
    void onPlaybackStateChanged(PlaybackStateCompat newPlaybackState) {
        //Do nothing
    }

    @Override
    void onMetadataChanged(MediaMetadataCompat newMetadata) {
        //Do nothing
    }

    @Override
    void onMediaBrowserConnected() {
        //Do nothing
    }

    private List<String> getSettings() {
        return Arrays.asList(SettingsStorageManager.KEY_THEME, SettingsStorageManager.KEY_SORT_BY_ARTIST, SettingsStorageManager.KEY_RECOGNIZE_NAMES);
    }
}
