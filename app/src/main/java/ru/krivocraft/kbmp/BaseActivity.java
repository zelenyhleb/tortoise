package ru.krivocraft.kbmp;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import ru.krivocraft.kbmp.constants.Constants;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme();
    }

    void setTheme() {
        SettingsManager settingsManager = new SettingsManager(this);
        boolean useLightTheme = settingsManager.getOption(Constants.KEY_THEME, false);

        if (useLightTheme) {
            setTheme(R.style.LightTheme);
        } else {
            setTheme(R.style.DarkTheme);
        }

    }
}
