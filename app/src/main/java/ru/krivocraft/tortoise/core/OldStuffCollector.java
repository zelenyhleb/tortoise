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

package ru.krivocraft.tortoise.core;

import android.content.Context;
import android.content.SharedPreferences;
import ru.krivocraft.tortoise.core.storage.PreferencesManager;
import ru.krivocraft.tortoise.core.storage.SettingsStorageManager;

import static android.content.Context.MODE_PRIVATE;

public class OldStuffCollector {
    private Context context;

    public OldStuffCollector(Context context) {
        this.context = context;
    }

    public void execute() {
        removeOldCache();
    }

    private void removeOldCache() {
        SharedPreferences preferences = context.getSharedPreferences(PreferencesManager.STORAGE_TRACK_LISTS, MODE_PRIVATE);
        String identifier = "all_tracks";
        if (preferences.getString(identifier, null) != null) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.remove(identifier);
            editor.apply();
        }
        SettingsStorageManager manager = new SettingsStorageManager(context);
        if (manager.getOption(SettingsStorageManager.KEY_OLD_TRACK_LISTS_EXIST, true)) {
            manager.putOption(SettingsStorageManager.KEY_OLD_TRACK_LISTS_EXIST, false);
        }
    }

}
