package ru.krivocraft.kbmp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import ru.krivocraft.kbmp.constants.Constants;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    private void switchTheme() {
        SharedPreferences settings = getSharedPreferences(Constants.SETTINGS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        if (settings.getBoolean(Constants.KEY_THEME, false)) {
            editor.putBoolean(Constants.KEY_THEME, false);
        } else {
            editor.putBoolean(Constants.KEY_THEME, true);
        }
        editor.apply();
    }
}
