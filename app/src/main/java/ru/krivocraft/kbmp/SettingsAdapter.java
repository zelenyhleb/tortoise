package ru.krivocraft.kbmp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import ru.krivocraft.kbmp.constants.Constants;

public class SettingsAdapter extends ArrayAdapter<String> {

    SettingsAdapter(@NonNull Context context) {
        super(context, R.layout.settings_item_toggle, new String[]{
                Constants.KEY_THEME,
                Constants.KEY_SORT_BY_ARTIST,
                Constants.KEY_SORT_BY_TAG,
                Constants.KEY_RECOGNIZE_NAMES,
                Constants.KEY_CLEAR_CACHE
        });
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.settings_item_toggle, null);
            if (position == 0) {
                TextView textView = convertView.findViewById(R.id.settings_text);
                textView.setText("Light theme (Beta)");
                Switch s = convertView.findViewById(R.id.settings_switch);
                initSwitch(s, Constants.KEY_THEME, false);
            } else if (position == 1) {
                Switch s = convertView.findViewById(R.id.settings_switch);
                TextView textView = convertView.findViewById(R.id.settings_text);
                initSwitch(s, Constants.KEY_SORT_BY_ARTIST, false);
                textView.setText("Automatically sort by artist");
            } else if (position == 2) {
                Switch s = convertView.findViewById(R.id.settings_switch);
                TextView textView = convertView.findViewById(R.id.settings_text);
                initSwitch(s, Constants.KEY_SORT_BY_TAG, false);
                textView.setText("Automatically sort by tag");
            } else if (position == 3) {
                Switch s = convertView.findViewById(R.id.settings_switch);
                TextView textView = convertView.findViewById(R.id.settings_text);
                initSwitch(s, Constants.KEY_RECOGNIZE_NAMES, true);
                textView.setText("Try to parse track names for tracks with no metadata");
            } else if (position == 4) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.settings_item_button, null);
                Button b = convertView.findViewById(R.id.settings_button);
                TextView textView = convertView.findViewById(R.id.settings_text);
                b.setText("CLEAR");
                textView.setText("Clear track lists cache");
                b.setOnClickListener(v -> {
                    Utils.clearCache(getContext().getSharedPreferences(Constants.STORAGE_TRACK_LISTS, Context.MODE_PRIVATE));
                    Toast.makeText(getContext(), "Cache cleared", Toast.LENGTH_LONG).show();
                });
            }
        }
        return convertView;
    }

    private void initSwitch(Switch s, String keyTheme, boolean defaultValue) {
        boolean useAlternativeTheme = Utils.getOption(getContext().getSharedPreferences(Constants.STORAGE_SETTINGS, Context.MODE_PRIVATE), keyTheme, defaultValue);
        s.setChecked(useAlternativeTheme);
        s.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (useAlternativeTheme) {
                Utils.putOption(getContext().getSharedPreferences(Constants.STORAGE_SETTINGS, Context.MODE_PRIVATE), keyTheme, false);
            } else {
                Utils.putOption(getContext().getSharedPreferences(Constants.STORAGE_SETTINGS, Context.MODE_PRIVATE), keyTheme, true);
            }
        });
    }

}
