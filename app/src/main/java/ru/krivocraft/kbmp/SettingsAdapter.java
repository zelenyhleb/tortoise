package ru.krivocraft.kbmp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

import java.util.List;

import ru.krivocraft.kbmp.constants.Constants;

public class SettingsAdapter extends ArrayAdapter<String> {

    private List<String> objects;
    private Activity context;

    SettingsAdapter(@NonNull Activity context, List<String> objects) {
        super(context, R.layout.settings_item_toggle, objects);
        this.context = context;
        this.objects = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.settings_item_toggle, null);
            if (position == objects.indexOf(Constants.KEY_THEME)) {
                TextView textView = convertView.findViewById(R.id.settings_text);
                textView.setText("Light theme (Beta)");
                Switch s = convertView.findViewById(R.id.settings_switch);
                initSwitch(s, Constants.KEY_THEME, false);
            } else if (position == objects.indexOf(Constants.KEY_SORT_BY_ARTIST)) {
                Switch s = convertView.findViewById(R.id.settings_switch);
                TextView textView = convertView.findViewById(R.id.settings_text);
                initSwitch(s, Constants.KEY_SORT_BY_ARTIST, false);
                textView.setText("Automatically sort by artist");
            } else if (position == objects.indexOf(Constants.KEY_SORT_BY_TAG)) {
                Switch s = convertView.findViewById(R.id.settings_switch);
                TextView textView = convertView.findViewById(R.id.settings_text);
                initSwitch(s, Constants.KEY_SORT_BY_TAG, false);
                textView.setText("Automatically sort by tag");
            } else if (position == objects.indexOf(Constants.KEY_RECOGNIZE_NAMES)) {
                Switch s = convertView.findViewById(R.id.settings_switch);
                TextView textView = convertView.findViewById(R.id.settings_text);
                initSwitch(s, Constants.KEY_RECOGNIZE_NAMES, true);
                textView.setText("Try to parse track names for tracks with no metadata");
            } else if (position == objects.indexOf(Constants.KEY_CLEAR_CACHE)) {
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
            if (keyTheme.equals(Constants.KEY_THEME)){
                context.finish();
                context.startActivity(new Intent(context, SettingsActivity.class));
            }
        });
    }

}
