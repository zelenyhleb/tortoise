/*
 * Copyright (c) 2019 Nikifor Fedorov
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *     SPDX-License-Identifier: Apache-2.0
 *     Contributors:
 * 	    Nikifor Fedorov - whole development
 */

package ru.krivocraft.tortoise.core.settings;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import ru.krivocraft.tortoise.R;

import java.util.List;

import static ru.krivocraft.tortoise.core.settings.SettingsStorageManager.*;

public class SettingsAdapter extends ArrayAdapter<String> {

    private final List<String> objects;
    private final SettingsStorageManager manager;

    public SettingsAdapter(@NonNull Activity context, List<String> objects) {
        super(context, R.layout.settings_item_toggle, objects);
        this.objects = objects;
        this.manager = new SettingsStorageManager(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View itemView;
        if (position < 3) {
            if (convertView == null) {
                itemView = LayoutInflater.from(getContext()).inflate(R.layout.settings_item_toggle, null);
                String key = objects.get(position);
                Switch s = itemView.findViewById(R.id.settings_switch);
                TextView textView = itemView.findViewById(R.id.settings_text);
                textView.setText(getDescription(key));
                initSwitch(s, key, getDefaultValue(key));
            } else {
                itemView = convertView;
            }
        } else {
            if (convertView == null) {
                itemView = LayoutInflater.from(getContext()).inflate(R.layout.settings_item_label, null);
                String key = objects.get(position);
                View layout = itemView.findViewById(R.id.menu_item);
                layout.setOnClickListener(view -> openURL(key));
                TextView textView = itemView.findViewById(R.id.settings_text);
                textView.setText(getDescription(key));
            } else {
                itemView = convertView;
            }
        }
        return itemView;
    }

    private String getDescription(String key) {
        switch (key) {
            case KEY_THEME:
                return getContext().getResources().getString(R.string.settings_theme);
            case KEY_RECOGNIZE_NAMES:
                return getContext().getResources().getString(R.string.settings_recognize);
            case KEY_WEBSITE:
                return getContext().getResources().getString(R.string.settings_website);
            case KEY_TELEGRAM:
                return getContext().getResources().getString(R.string.settings_telegram);
            case KEY_SHOW_IGNORED:
                return getContext().getResources().getString(R.string.show_hidden_tracks);
            case KEY_HELP:
                return getContext().getResources().getString(R.string.link_help);
            default:
                return getContext().getResources().getString(R.string.settings_item_default);
        }
    }

    private void openURL(String url) {
        Uri uriUrl = Uri.parse(url);
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
        getContext().startActivity(launchBrowser);
    }

    private boolean getDefaultValue(String key) {
        return key.equals(KEY_RECOGNIZE_NAMES);
    }

    private void initSwitch(Switch s, String key, boolean defaultValue) {
        boolean option = manager.getOption(key, defaultValue);
        s.setChecked(option);
        s.setOnCheckedChangeListener((buttonView, isChecked) -> {
            manager.putOption(key, !option);
            if (key.equals(SettingsStorageManager.KEY_RECOGNIZE_NAMES) || key.equals(KEY_THEME)) {
                Toast.makeText(getContext(), "You will see changes after app restarting", Toast.LENGTH_LONG).show();
            }
        });
    }

}
