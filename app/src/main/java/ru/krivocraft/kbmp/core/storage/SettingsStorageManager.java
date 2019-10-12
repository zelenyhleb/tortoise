package ru.krivocraft.kbmp.core.storage;

import android.content.Context;

public class SettingsStorageManager {

    public static final String KEY_THEME = "useAlternativeTheme";
    public static final String KEY_SORT_BY_ARTIST = "sortByArtist";
    public static final String KEY_SORT_BY_TAG = "sortByTag";
    public static final String KEY_CLEAR_CACHE = "clearCache";
    public static final String KEY_RECOGNIZE_NAMES = "recognizeNames";
    public static final String KEY_OLD_TRACK_LISTS_EXIST = "oldExist";

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
