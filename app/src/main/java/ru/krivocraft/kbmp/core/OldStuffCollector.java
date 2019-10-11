package ru.krivocraft.kbmp.core;

import android.content.Context;
import android.content.SharedPreferences;

import ru.krivocraft.kbmp.constants.Constants;
import ru.krivocraft.kbmp.core.settings.SettingsManager;

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
        SharedPreferences preferences = context.getSharedPreferences(Constants.STORAGE_TRACK_LISTS, MODE_PRIVATE);
        String identifier = "all_tracks";
        if (preferences.getString(identifier, null) != null) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.remove(identifier);
            editor.apply();
        }
        SettingsManager manager = new SettingsManager(context);
        if (manager.getOption(Constants.KEY_OLD_TRACK_LISTS_EXIST, true)) {
            manager.putOption(Constants.KEY_OLD_TRACK_LISTS_EXIST, false);
        }
    }

}
