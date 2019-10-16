package ru.krivocraft.kbmp.core.storage;

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
