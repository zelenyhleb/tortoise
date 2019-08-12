package ru.krivocraft.kbmp;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Switch;
import android.widget.TextView;

import ru.krivocraft.kbmp.constants.Constants;

public class SettingsAdapter extends ArrayAdapter<String> {

    SettingsAdapter(@NonNull Context context) {
        super(context, R.layout.settings_item, new String[]{
                Constants.KEY_THEME,
                Constants.KEY_AUTO_SORT,
                Constants.KEY_RECOGNIZE_NAMES
        });
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.settings_item, null);
            Switch s = convertView.findViewById(R.id.settings_switch);
            TextView textView = convertView.findViewById(R.id.settings_text);

            if (position == 0) {
                s.setChecked(true);
            } else if (position == 1) {
                boolean autoSort = Utils.getOption(getContext().getSharedPreferences(Constants.SETTINGS_NAME, Context.MODE_PRIVATE), Constants.KEY_AUTO_SORT);
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
                boolean recognize = Utils.getOption(getContext().getSharedPreferences(Constants.SETTINGS_NAME, Context.MODE_PRIVATE), Constants.KEY_RECOGNIZE_NAMES);
                s.setChecked(recognize);
                s.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (recognize) {
                        Utils.putOption(getContext().getSharedPreferences(Constants.SETTINGS_NAME, Context.MODE_PRIVATE), Constants.KEY_RECOGNIZE_NAMES, false);
                    } else {
                        Utils.putOption(getContext().getSharedPreferences(Constants.SETTINGS_NAME, Context.MODE_PRIVATE), Constants.KEY_RECOGNIZE_NAMES, true);
                    }
                });
                textView.setText("Try to parse track names for tracks with no metadata");
            }
        }
        return convertView;
    }
}
