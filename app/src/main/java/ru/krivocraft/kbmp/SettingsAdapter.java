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

    public SettingsAdapter(@NonNull Context context) {
        super(context, R.layout.settings_item, new String[]{
                Constants.KEY_THEME,
                Constants.KEY_AUTO_SORT
        });
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.settings_item, null);
            if (position == 0) {
                convertView.findViewById(R.id.settings_switch).setEnabled(false);
            } else {
                SharedPreferences preferences = getContext().getSharedPreferences(Constants.SETTINGS_NAME, Context.MODE_PRIVATE);

                Switch s = convertView.findViewById(R.id.settings_switch);
                boolean autoSort = preferences.getBoolean(Constants.KEY_AUTO_SORT, false);

                s.setChecked(autoSort);
                s.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    SharedPreferences.Editor editor = preferences.edit();
                    if (autoSort) {
                        editor.putBoolean(Constants.KEY_AUTO_SORT, false);
                    } else {
                        editor.putBoolean(Constants.KEY_AUTO_SORT, true);
                    }
                    editor.apply();
                });

                TextView textView = convertView.findViewById(R.id.settings_text);
                textView.setText("Sort by artist");
            }
        }
        return convertView;
    }
}
