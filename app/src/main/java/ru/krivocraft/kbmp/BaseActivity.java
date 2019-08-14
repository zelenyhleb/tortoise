package ru.krivocraft.kbmp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import ru.krivocraft.kbmp.constants.Constants;

public abstract class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Utils.getOption(getSharedPreferences(Constants.STORAGE_SETTINGS, MODE_PRIVATE), Constants.KEY_THEME, false)) {
            setTheme(R.style.LightTheme);
        } else {
            setTheme(R.style.DarkTheme);
        }
    }
}
