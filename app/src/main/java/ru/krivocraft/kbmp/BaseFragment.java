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

    public BaseFragment(){
        //Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();
        invalidate();
    }

    abstract void invalidate();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Context context = getContext();
        if (context != null) {
            if (Utils.getOption(context.getSharedPreferences(Constants.STORAGE_SETTINGS, Context.MODE_PRIVATE), Constants.KEY_THEME, false)){
                context.getTheme().applyStyle(R.style.LightTheme, true);
            } else {
                context.getTheme().applyStyle(R.style.DarkTheme, true);
            }
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
