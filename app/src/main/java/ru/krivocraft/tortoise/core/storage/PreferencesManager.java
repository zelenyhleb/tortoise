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
import android.content.SharedPreferences;

public class PreferencesManager {
    public static final String STORAGE_TRACK_LISTS = "trackLists";
    public static final String STORAGE_SETTINGS = "settings";

    private final SharedPreferences preferences;

    public PreferencesManager(Context context, String preferencesName) {
        this.preferences = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE);
    }

    public void writeBoolean(String key, boolean value) {
        getEditor().putBoolean(key, value).apply();
    }

    public void writeInt(String key, int value) {
        getEditor().putInt(key, value).apply();
    }

    public boolean readBoolean(String key, boolean defaultValue) {
        return preferences.getBoolean(key, defaultValue);
    }

    public int readInt(String key) {
        return preferences.getInt(key, 0);
    }

    private SharedPreferences.Editor getEditor() {
        return preferences.edit();
    }
}
