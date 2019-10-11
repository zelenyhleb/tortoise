package ru.krivocraft.kbmp.core.settings;

import android.content.Context;
import android.content.SharedPreferences;

import ru.krivocraft.kbmp.constants.Constants;

public class SettingsManager {
    private Context context;

    public SettingsManager(Context context) {
        this.context = context;
    }

    public boolean getOption(String key, boolean defValue) {
        return getSharedPreferences().getBoolean(key, defValue);
    }

    public void putOption(String key, boolean value) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    private SharedPreferences getSharedPreferences() {
        return context.getSharedPreferences(Constants.STORAGE_SETTINGS, Context.MODE_PRIVATE);
    }
}
