package ru.krivocraft.kbmp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ListView;

import ru.krivocraft.kbmp.constants.Constants;

public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ListView listView = findViewById(R.id.settings_list);
        listView.setAdapter(new SettingsAdapter(this));
    }

    private void switchTheme() {
        SharedPreferences settings = getSharedPreferences(Constants.STORAGE_SETTINGS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        if (settings.getBoolean(Constants.KEY_THEME, false)) {
            editor.putBoolean(Constants.KEY_THEME, false);
        } else {
            editor.putBoolean(Constants.KEY_THEME, true);
        }
        editor.apply();
    }
}
