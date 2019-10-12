package ru.krivocraft.kbmp.contexts;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import ru.krivocraft.kbmp.R;
import ru.krivocraft.kbmp.core.storage.SettingsStorageManager;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme();
    }

    void setTheme() {
        SettingsStorageManager settingsManager = new SettingsStorageManager(this);
        boolean useLightTheme = settingsManager.getOption(SettingsStorageManager.KEY_THEME, false);

        if (useLightTheme) {
            setTheme(R.style.LightTheme);
        } else {
            setTheme(R.style.DarkTheme);
        }

    }
}
