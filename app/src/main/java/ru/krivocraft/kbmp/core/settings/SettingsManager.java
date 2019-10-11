package ru.krivocraft.kbmp.core.settings;

import android.content.Context;

import ru.krivocraft.kbmp.constants.Constants;
import ru.krivocraft.kbmp.core.storage.PreferencesManager;

public class SettingsManager {
    private final PreferencesManager preferencesManager;

    public SettingsManager(Context context) {
        this.preferencesManager = new PreferencesManager(context, Constants.STORAGE_SETTINGS);
    }

    public boolean getOption(String key, boolean defValue) {
        return preferencesManager.readBoolean(key, defValue);
    }

    public void putOption(String key, boolean value) {
        preferencesManager.writeBoolean(key, value);
    }
}
