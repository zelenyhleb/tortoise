package ru.krivocraft.kbmp.core.settings;

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

import ru.krivocraft.kbmp.R;
import ru.krivocraft.kbmp.core.storage.SettingsStorageManager;

public class SettingsAdapter extends ArrayAdapter<String> {

    private List<String> objects;
    private SettingsStorageManager manager;

    public SettingsAdapter(@NonNull Activity context, List<String> objects) {
        super(context, R.layout.settings_item_toggle, objects);
        this.objects = objects;
        this.manager = new SettingsStorageManager(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View itemView;
        if (convertView == null) {
            itemView = LayoutInflater.from(getContext()).inflate(R.layout.settings_item_toggle, null);
            if (position == objects.indexOf(SettingsStorageManager.KEY_THEME)) {
                TextView textView = itemView.findViewById(R.id.settings_text);
                textView.setText(R.string.settings_theme);
                Switch s = itemView.findViewById(R.id.settings_switch);
                initSwitch(s, SettingsStorageManager.KEY_THEME, false);
            } else if (position == objects.indexOf(SettingsStorageManager.KEY_SORT_BY_ARTIST)) {
                Switch s = itemView.findViewById(R.id.settings_switch);
                TextView textView = itemView.findViewById(R.id.settings_text);
                initSwitch(s, SettingsStorageManager.KEY_SORT_BY_ARTIST, false);
                textView.setText(R.string.settings_sort_artist);
            } else if (position == objects.indexOf(SettingsStorageManager.KEY_RECOGNIZE_NAMES)) {
                Switch s = itemView.findViewById(R.id.settings_switch);
                TextView textView = itemView.findViewById(R.id.settings_text);
                initSwitch(s, SettingsStorageManager.KEY_RECOGNIZE_NAMES, true);
                textView.setText(R.string.settings_recognize);
            }
        } else {
            itemView = convertView;
        }
        return itemView;
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
            if (key.equals(SettingsStorageManager.KEY_RECOGNIZE_NAMES) || key.equals(SettingsStorageManager.KEY_THEME)) {
                Toast.makeText(getContext(), "You will see changes after app restarting", Toast.LENGTH_LONG).show();
            }
        });
    }

}
