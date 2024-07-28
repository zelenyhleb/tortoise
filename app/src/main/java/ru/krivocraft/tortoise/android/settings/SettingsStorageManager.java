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

package ru.krivocraft.tortoise.android.settings;

import java.util.Optional;

import ru.krivocraft.tortoise.core.api.settings.WriteableSettings;

public class SettingsStorageManager {

    public static final String KEY_WEBSITE = "https://zelenyhleb.ru/";
    public static final String KEY_TELEGRAM = "https://t.me/krivocraft/";
    public static final String KEY_HELP = "https://github.com/zelenyhleb/tortoise/wiki/Usage";
    public static final String KEY_SHOW_IGNORED = "showIgnored";
    public static final String KEY_RECOGNIZE_NAMES = "recognizeNames";

    private final WriteableSettings settings;

    public SettingsStorageManager(WriteableSettings settings) {
        this.settings = settings;
    }

    public boolean get(String key, boolean defValue) {
        return settings.read(key, defValue);
    }

    public Optional<String> getString(String key) {
        return Optional.ofNullable(settings.read(key, null));
    }

    public void putString(String key, String value) {
        settings.write(key, value);
    }

    public void put(String key, boolean value) {
        settings.write(key, value);
    }
}
