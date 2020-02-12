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

import android.media.audiofx.Equalizer;
import ru.krivocraft.tortoise.fragments.audiofx.EqualizerManager;

public class EqualizerStorageManager {
    private final PreferencesManager preferencesManager;

    public EqualizerStorageManager(PreferencesManager preferencesManager) {
        this.preferencesManager = preferencesManager;
    }

    public void writeSettings(Equalizer.Settings settings) {
        preferencesManager.writeInt(EqualizerManager.EXTRA_NUMBER_OF_BANDS, settings.numBands);
        for (int i = 0; i < settings.numBands; i++) {
            preferencesManager.writeInt(String.valueOf(i), settings.bandLevels[i]);
        }
    }

    public Equalizer.Settings readSettings() {
        int numBands = preferencesManager.readInt(EqualizerManager.EXTRA_NUMBER_OF_BANDS);
        short[] levels = new short[numBands];
        for (int i = 0; i < numBands; i++) {
            levels[i] = (short) preferencesManager.readInt(String.valueOf(i));
        }
        Equalizer.Settings settings = new Equalizer.Settings();
        settings.numBands = (short) numBands;
        settings.bandLevels = levels;
        return settings;
    }
}
