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

package ru.krivocraft.tortoise.core.player;

import android.content.SharedPreferences;
import ru.krivocraft.tortoise.core.api.settings.WriteableSettings;

public class SharedPreferencesSettings extends WriteableSettings {

    private final SharedPreferences preferences;

    public SharedPreferencesSettings(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    @Override
    public boolean read(String key, boolean defaultValue) {
        return preferences.getBoolean(key, defaultValue);
    }

    @Override
    public int read(String key, int defaultValue) {
        return preferences.getInt(key, defaultValue);
    }

    @Override
    public String read(String key, String defaultValue) {
        return preferences.getString(key, defaultValue);
    }

    @Override
    public void write(String key, boolean value) {
        preferences.edit().putBoolean(key, value).apply();
    }

    @Override
    public void write(String key, int value) {
        preferences.edit().putInt(key, value).apply();
    }

    @Override
    public void write(String key, String value) {
        preferences.edit().putString(key, value).apply();
    }
}
