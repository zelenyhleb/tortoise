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

package ru.krivocraft.tortoise.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import ru.krivocraft.tortoise.R;
import ru.krivocraft.tortoise.core.storage.SettingsStorageManager;

public abstract class BaseFragment extends Fragment {

    private SettingsStorageManager settingsManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        invalidate();
    }

    public abstract void invalidate();

    public void onBackPressed() {
        //This callback can be implemented by inheritors
    }

    public SettingsStorageManager getSettingsManager() {
        return settingsManager;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Context context = getContext();
        if (context != null) {
            settingsManager = new SettingsStorageManager(context);
            if (settingsManager.getOption(SettingsStorageManager.KEY_THEME, false)) {
                context.getTheme().applyStyle(R.style.LightTheme, true);
            } else {
                context.getTheme().applyStyle(R.style.DarkTheme, true);
            }
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
