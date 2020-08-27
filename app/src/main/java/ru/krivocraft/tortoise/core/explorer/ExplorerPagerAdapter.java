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

package ru.krivocraft.tortoise.core.explorer;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import ru.krivocraft.tortoise.R;
import ru.krivocraft.tortoise.core.model.TrackList;

import java.util.List;

class ExplorerPagerAdapter extends FragmentPagerAdapter {

    private final TrackListsGridFragment customTrackLists;
    private final TrackListsGridFragment sortedTrackLists;
    private final Context context;

    ExplorerPagerAdapter(@NonNull FragmentManager fm,
                         TrackListsGridFragment.OnItemClickListener listener,
                         List<TrackList> custom, List<TrackList> sorted, Context context) {
        super(fm);
        this.customTrackLists = TrackListsGridFragment.newInstance(listener, custom, context);
        this.sortedTrackLists = TrackListsGridFragment.newInstance(listener, sorted, context);
        this.context = context;
    }

    public final void setTrackLists(List<TrackList> custom, List<TrackList> sorted) {
        customTrackLists.setTrackLists(custom);
        sortedTrackLists.setTrackLists(sorted);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return customTrackLists;
        } else {
            return sortedTrackLists;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            return context.getResources().getString(R.string.tab_title_custom);
        } else {
            return context.getResources().getString(R.string.tab_title_sorted);
        }
    }
}
