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

public class SettingsStorageManager {

    public static final String KEY_THEME = "useAlternativeTheme";
    public static final String KEY_SORT_BY_ARTIST = "sortByArtist";
    public static final String KEY_SORT_BY_TAG = "sortByTag";
    public static final String KEY_CLEAR_CACHE = "clearCache";
    public static final String KEY_RECOGNIZE_NAMES = "recognizeNames";
    public static final String KEY_OLD_TRACK_LISTS_EXIST = "oldExist";
    public static final String KEY_THUMBNAILS_CLEARED = "thumbs";

    private final PreferencesManager preferencesManager;

    public SettingsStorageManager(Context context) {
        this.preferencesManager = new PreferencesManager(context, PreferencesManager.STORAGE_SETTINGS);
    }

    public boolean getOption(String key, boolean defValue) {
        return preferencesManager.readBoolean(key, defValue);
    }

    public void putOption(String key, boolean value) {
        preferencesManager.writeBoolean(key, value);
    }
}
