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
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import ru.krivocraft.tortoise.R;
import ru.krivocraft.tortoise.core.BaseFragment;

import java.util.Arrays;
import java.util.List;

public class SettingsFragment extends BaseFragment {

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        ListView listView = rootView.findViewById(R.id.settings_list);
        List<String> settings = getSettings();
        Activity context = getActivity();
        if (context != null) {
            listView.setAdapter(new SettingsAdapter(context, settings));
        }
        return rootView;
    }

    private List<String> getSettings() {
        return Arrays.asList(SettingsStorageManager.KEY_THEME,
                SettingsStorageManager.KEY_RECOGNIZE_NAMES,
                SettingsStorageManager.KEY_SHOW_IGNORED,
                SettingsStorageManager.KEY_WEBSITE,
                SettingsStorageManager.KEY_TELEGRAM,
                SettingsStorageManager.KEY_HELP);
    }

    @Override
    public void invalidate() {
        //Do nothing yet
    }

    @Override
    public void changeColors(int color) {
        //Do nothing yet
    }
}
