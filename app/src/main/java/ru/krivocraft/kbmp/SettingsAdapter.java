package ru.krivocraft.kbmp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import ru.krivocraft.kbmp.constants.Constants;

public class SettingsAdapter extends ArrayAdapter<String> {

    SettingsAdapter(@NonNull Context context) {
        super(context, R.layout.settings_item_toggle, new String[]{
                Constants.KEY_THEME,
                Constants.KEY_AUTO_SORT,
                Constants.KEY_RECOGNIZE_NAMES,
                Constants.KEY_CLEAR_CACHE
        });
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            if (position == 0) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.settings_item_toggle, null);
                TextView textView = convertView.findViewById(R.id.settings_text);
                textView.setText("Light theme (Beta)");
                Switch s = convertView.findViewById(R.id.settings_switch);
                boolean useAlternativeTheme = Utils.getOption(getContext().getSharedPreferences(Constants.SETTINGS_NAME, Context.MODE_PRIVATE), Constants.KEY_THEME, false);
                s.setChecked(useAlternativeTheme);
                s.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (useAlternativeTheme) {
                        Utils.putOption(getContext().getSharedPreferences(Constants.SETTINGS_NAME, Context.MODE_PRIVATE), Constants.KEY_THEME, false);
                    } else {
                        Utils.putOption(getContext().getSharedPreferences(Constants.SETTINGS_NAME, Context.MODE_PRIVATE), Constants.KEY_THEME, true);
                    }
//                    Utils.restart(getContext());
                });
            } else if (position == 1) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.settings_item_toggle, null);
                Switch s = convertView.findViewById(R.id.settings_switch);
                TextView textView = convertView.findViewById(R.id.settings_text);
                boolean autoSort = Utils.getOption(getContext().getSharedPreferences(Constants.SETTINGS_NAME, Context.MODE_PRIVATE), Constants.KEY_AUTO_SORT, false);
                s.setChecked(autoSort);
                s.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (autoSort) {
                        Utils.putOption(getContext().getSharedPreferences(Constants.SETTINGS_NAME, Context.MODE_PRIVATE), Constants.KEY_AUTO_SORT, false);
                    } else {
                        Utils.putOption(getContext().getSharedPreferences(Constants.SETTINGS_NAME, Context.MODE_PRIVATE), Constants.KEY_AUTO_SORT, true);
                    }
                });
                textView.setText("Sort by artist");
            } else if (position == 2) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.settings_item_toggle, null);
                Switch s = convertView.findViewById(R.id.settings_switch);
                TextView textView = convertView.findViewById(R.id.settings_text);
                boolean recognize = Utils.getOption(getContext().getSharedPreferences(Constants.SETTINGS_NAME, Context.MODE_PRIVATE), Constants.KEY_RECOGNIZE_NAMES, true);
                s.setChecked(recognize);
                s.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (recognize) {
                        Utils.putOption(getContext().getSharedPreferences(Constants.SETTINGS_NAME, Context.MODE_PRIVATE), Constants.KEY_RECOGNIZE_NAMES, false);
                    } else {
                        Utils.putOption(getContext().getSharedPreferences(Constants.SETTINGS_NAME, Context.MODE_PRIVATE), Constants.KEY_RECOGNIZE_NAMES, true);
                    }
                });
                textView.setText("Try to parse track names for tracks with no metadata");
            } else if (position == 3) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.settings_item_button, null);
                Button b = convertView.findViewById(R.id.settings_button);
                TextView textView = convertView.findViewById(R.id.settings_text);
                b.setText("CLEAR");
                textView.setText("Clear track lists cache");
                b.setOnClickListener(v -> {
                    Utils.clearCache(getContext().getSharedPreferences(Constants.TRACK_LISTS_NAME, Context.MODE_PRIVATE));
                    Toast.makeText(getContext(), "Cache cleared", Toast.LENGTH_LONG).show();
//                    Utils.restart(getContext());
                });
            }
        }
        return convertView;
    }

}
