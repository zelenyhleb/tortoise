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

package ru.krivocraft.tortoise.core.settings;

import android.content.Context;
import ru.krivocraft.tortoise.core.PreferencesManager;

public class SettingsStorageManager {

    public static final String KEY_THEME = "useAlternativeTheme";
    public static final String KEY_WEBSITE = "https://krivocraft.ru/";
    public static final String KEY_TELEGRAM = "https://t.me/krivocraft/";
    public static final String KEY_HELP = "https://github.com/zelenyhleb/tortoise/wiki/Usage";
    public static final String KEY_ALTERNATIVE_SEEK = "alternativeSeek";
    public static final String KEY_SHOW_IGNORED = "showIgnored";
    public static final String KEY_SMART_SHUFFLE = "smartShuffle";
    public static final String KEY_RECOGNIZE_NAMES = "recognizeNames";

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
