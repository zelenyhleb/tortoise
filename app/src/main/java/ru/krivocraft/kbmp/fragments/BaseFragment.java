package ru.krivocraft.kbmp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ru.krivocraft.kbmp.R;
import ru.krivocraft.kbmp.core.storage.SettingsStorageManager;

public abstract class BaseFragment extends Fragment {

    private SettingsStorageManager settingsManager;

    @Override
    public void onResume() {
        super.onResume();
        invalidate();
    }

    public abstract void invalidate();

    public SettingsStorageManager getSettingsManager() {
        return settingsManager;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Context context = getContext();
        if (context != null) {
            settingsManager = new SettingsStorageManager(context);
            if (settingsManager.getOption(SettingsStorageManager.KEY_THEME, false)) {
                context.getTheme().applyStyle(R.style.LightTheme, true);
            } else {
                context.getTheme().applyStyle(R.style.DarkTheme, true);
            }
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
