package ru.krivocraft.kbmp;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import ru.krivocraft.kbmp.constants.Constants;

public class SettingsAdapter extends ArrayAdapter<String> {

    private List<String> objects;
    private SettingsManager manager;

    SettingsAdapter(@NonNull Activity context, List<String> objects) {
        super(context, R.layout.settings_item_toggle, objects);
        this.objects = objects;
        this.manager = new SettingsManager(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.settings_item_toggle, null);
            if (position == objects.indexOf(Constants.KEY_THEME)) {
                TextView textView = convertView.findViewById(R.id.settings_text);
                textView.setText(R.string.settings_theme);
                Switch s = convertView.findViewById(R.id.settings_switch);
                initSwitch(s, Constants.KEY_THEME, false);
            } else if (position == objects.indexOf(Constants.KEY_SORT_BY_ARTIST)) {
                Switch s = convertView.findViewById(R.id.settings_switch);
                TextView textView = convertView.findViewById(R.id.settings_text);
                initSwitch(s, Constants.KEY_SORT_BY_ARTIST, false);
                textView.setText(R.string.settings_sort_artist);
            } else if (position == objects.indexOf(Constants.KEY_RECOGNIZE_NAMES)) {
                Switch s = convertView.findViewById(R.id.settings_switch);
                TextView textView = convertView.findViewById(R.id.settings_text);
                initSwitch(s, Constants.KEY_RECOGNIZE_NAMES, true);
                textView.setText(R.string.settings_recognize);
            }
        }
        return convertView;
    }

    private void initSwitch(Switch s, String key, boolean defaultValue) {
        boolean option = manager.getOption(key, defaultValue);
        s.setChecked(option);
        s.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (option) {
                manager.putOption(key, false);
            } else {
                manager.putOption(key, true);
            }
            if (key.equals(Constants.KEY_RECOGNIZE_NAMES) || key.equals(Constants.KEY_THEME)) {
                Toast.makeText(getContext(), "You will see changes after app restarting", Toast.LENGTH_LONG).show();
            }
        });
    }

}
