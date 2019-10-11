package ru.krivocraft.kbmp.core.storage;

import android.media.audiofx.Equalizer;

import ru.krivocraft.kbmp.core.audiofx.EqualizerManager;

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
