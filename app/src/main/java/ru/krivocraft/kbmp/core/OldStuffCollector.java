package ru.krivocraft.kbmp.core;

import android.content.Context;
import android.content.SharedPreferences;

import ru.krivocraft.kbmp.core.storage.PreferencesManager;
import ru.krivocraft.kbmp.core.storage.SettingsStorageManager;

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
