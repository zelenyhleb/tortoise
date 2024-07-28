/*
 * Copyright (c) 2020 Nikifor Fedorov
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
 *         Nikifor Fedorov and others
 */

package ru.krivocraft.tortoise.android.settings;

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
import ru.krivocraft.tortoise.android.player.SharedPreferencesSettings;
import ru.krivocraft.tortoise.core.rating.Shuffle;

import java.util.List;

import static ru.krivocraft.tortoise.android.settings.SettingsStorageManager.*;

public class SettingsAdapter extends ArrayAdapter<String> {

    private final List<String> objects;
    private final SettingsStorageManager manager;

    public SettingsAdapter(@NonNull Activity context, List<String> objects) {
        super(context, R.layout.settings_item_toggle, objects);
        this.objects = objects;
        this.manager = new SettingsStorageManager(new SharedPreferencesSettings(context));
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String key = objects.get(position);
        if (convertView != null) {
            return convertView;
        }
        return switch (key) {
            case KEY_RECOGNIZE_NAMES,
                 Shuffle.KEY_SMART_SHUFFLE,
                 KEY_SHOW_IGNORED -> toggleView(key);
            default -> labelView(key);
        };
    }

    private @NonNull View labelView(String key) {
        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.settings_item_label, null);
        View layout = itemView.findViewById(R.id.menu_item);
        layout.setOnClickListener(view -> openURL(key));
        TextView textView = itemView.findViewById(R.id.settings_text);
        textView.setText(getDescription(key));
        return itemView;
    }

    private @NonNull View toggleView(String key) {
        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.settings_item_toggle, null);
        Switch s = itemView.findViewById(R.id.settings_switch);
        TextView textView = itemView.findViewById(R.id.settings_text);
        textView.setText(getDescription(key));
        initSwitch(s, key, getDefaultValue(key));
        return itemView;
    }

    private String getDescription(String key) {
        return switch (key) {
            case KEY_RECOGNIZE_NAMES ->
                    getContext().getResources().getString(R.string.settings_recognize);
            case Shuffle.KEY_SMART_SHUFFLE ->
                    getContext().getResources().getString(R.string.settings_smart_shuffle);
            case KEY_WEBSITE -> getContext().getResources().getString(R.string.settings_website);
            case KEY_TELEGRAM -> getContext().getResources().getString(R.string.settings_telegram);
            case KEY_SHOW_IGNORED ->
                    getContext().getResources().getString(R.string.show_hidden_tracks);
            case KEY_HELP -> getContext().getResources().getString(R.string.link_help);
            default -> getContext().getResources().getString(R.string.settings_item_default);
        };
    }

    private void openURL(String url) {
        Uri uriUrl = Uri.parse(url);
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
        getContext().startActivity(launchBrowser);
    }

    private boolean getDefaultValue(String key) {
        return key.equals(KEY_RECOGNIZE_NAMES) || key.equals(Shuffle.KEY_SMART_SHUFFLE);
    }

    private void initSwitch(Switch s, String key, boolean defaultValue) {
        boolean option = manager.get(key, defaultValue);
        s.setChecked(option);
        s.setOnCheckedChangeListener((buttonView, isChecked) -> {
            manager.put(key, !option);
            if (key.equals(SettingsStorageManager.KEY_RECOGNIZE_NAMES)) {
                Toast.makeText(getContext(), "You will see changes after app restarting", Toast.LENGTH_LONG).show();
            }
        });
    }

}
