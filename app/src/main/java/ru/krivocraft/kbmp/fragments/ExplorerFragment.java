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

package ru.krivocraft.kbmp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import ru.krivocraft.kbmp.R;
import ru.krivocraft.kbmp.core.storage.TrackListsStorageManager;

public class ExplorerFragment extends BaseFragment {

    private TrackListStackFragment.OnItemClickListener listener;

    public static ExplorerFragment newInstance(TrackListStackFragment.OnItemClickListener listener) {
        ExplorerFragment explorerFragment = new ExplorerFragment();
        explorerFragment.setListener(listener);
        return explorerFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_explorer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        PagerAdapter adapter = new PagerAdapter(getChildFragmentManager());
        ViewPager pager = view.findViewById(R.id.explorer_pager);
        pager.setAdapter(adapter);
        TabLayout tabLayout = view.findViewById(R.id.explorer_tabs);
        tabLayout.setupWithViewPager(pager);
    }

    @Override
    public void invalidate() {
        //Do nothing
    }

    private TrackListStackFragment getCustom() {
        return TrackListStackFragment.newInstance(listener, TrackListsStorageManager.FILTER_CUSTOM);
    }

    private TrackListStackFragment getSortByArtist() {
        return TrackListStackFragment.newInstance(listener, TrackListsStorageManager.FILTER_ARTIST);
    }

    public void setListener(TrackListStackFragment.OnItemClickListener listener) {
        this.listener = listener;
    }

    private class PagerAdapter extends FragmentPagerAdapter {

        private PagerAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return getCustom();
            } else {
                return getSortByArtist();
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
                return "Custom";
            } else {
                return "Sorted by artist";
            }
        }
    }
}
