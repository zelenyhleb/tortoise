package ru.krivocraft.kbmp;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ru.krivocraft.kbmp.constants.Constants;

public abstract class BaseFragment extends Fragment {

    private SettingsManager settingsManager;

    @Override
    public void onResume() {
        super.onResume();
        invalidate();
    }

    abstract void invalidate();

    public SettingsManager getSettingsManager() {
        return settingsManager;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Context context = getContext();
        if (context != null) {
            settingsManager = new SettingsManager(context);
            if (settingsManager.getOption(Constants.KEY_THEME, false)){
                context.getTheme().applyStyle(R.style.LightTheme, true);
            } else {
                context.getTheme().applyStyle(R.style.DarkTheme, true);
            }
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
