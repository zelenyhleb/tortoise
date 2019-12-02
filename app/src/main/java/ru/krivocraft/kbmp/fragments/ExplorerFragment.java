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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import ru.krivocraft.kbmp.R;
import ru.krivocraft.kbmp.contexts.TrackListEditorActivity;
import ru.krivocraft.kbmp.core.TrackListsCompiler;
import ru.krivocraft.kbmp.core.storage.SettingsStorageManager;
import ru.krivocraft.kbmp.core.storage.TrackListsStorageManager;
import ru.krivocraft.kbmp.core.track.TrackList;

public class ExplorerFragment extends BaseFragment {

    private TrackListStackFragment.OnItemClickListener listener;
    private TrackListsStorageManager tracksStorageManager;
    private TrackListsCompiler trackListsCompiler;

    private TrackListStackFragment customFragment;
    private TrackListStackFragment artistFragment;
    private PagerAdapter adapter;

    public static ExplorerFragment newInstance(TrackListStackFragment.OnItemClickListener listener) {
        ExplorerFragment explorerFragment = new ExplorerFragment();
        explorerFragment.setListener(listener);
        return explorerFragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_explorer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        new Thread(() -> {
            FloatingActionButton button = view.findViewById(R.id.add_track_list_button);

            FragmentActivity activity = getActivity();
            if (activity != null) {
                configureAddButton(view, activity);
                this.tracksStorageManager = new TrackListsStorageManager(activity, TrackListsStorageManager.FILTER_ALL);
                this.trackListsCompiler = new TrackListsCompiler(activity);
            }

            ViewPager pager = view.findViewById(R.id.explorer_pager);
            pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    //Do nothing
                }

                @Override
                public void onPageSelected(int position) {
                    if (position > 0) {
                        button.hide();
                    } else {
                        button.show();
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                    //Do nothing
                }
            });
            adapter = new PagerAdapter(getChildFragmentManager());

            TabLayout tabLayout = view.findViewById(R.id.explorer_tabs);

            invalidate();

            if (activity != null) {
                activity.runOnUiThread(() -> {
                    view.findViewById(R.id.explorer_progress).setVisibility(View.GONE);
                    pager.setAdapter(adapter);
                    tabLayout.setupWithViewPager(pager);
                });
            }
        }).start();

    }

    private void onNewTrackLists(List<TrackList> trackLists) {
        Activity activity = getActivity();
        if (activity != null) {
            for (TrackList trackList : trackLists) {
                if (tracksStorageManager.getExistingTrackListNames().contains(trackList.getDisplayName())) {
                    tracksStorageManager.updateTrackListContent(trackList);
                } else {
                    tracksStorageManager.writeTrackList(trackList);

                }
            }
            if (artistFragment != null) {
                activity.runOnUiThread(artistFragment::invalidate);
            }

            if (customFragment != null) {
                activity.runOnUiThread(customFragment::invalidate);
            }
        }
    }

    @Override
    public void invalidate() {
        Context context = getContext();
        if (context != null) {
            if (trackListsCompiler != null) {
                trackListsCompiler.compileFavorites(this::onNewTrackLists);
                if (getSettingsManager().getOption(SettingsStorageManager.KEY_SORT_BY_ARTIST, false)) {
                    trackListsCompiler.compileByAuthors(this::onNewTrackLists);
                }
            }
        }
    }

    private void configureAddButton(View rootView, Context context) {
        FloatingActionButton addTrackList = rootView.findViewById(R.id.add_track_list_button);
        addTrackList.setOnClickListener(v -> showCreationDialog(context));
    }

    private void showCreationDialog(Context context) {
        Intent intent = new Intent(context, TrackListEditorActivity.class);
        intent.putExtra(TrackList.EXTRA_TRACK_LIST, new TrackList("", new ArrayList<>(), TrackList.TRACK_LIST_CUSTOM).toJson());
        intent.putExtra(TrackListEditorActivity.EXTRA_CREATION, true);
        context.startActivity(intent);
    }

    private TrackListStackFragment getCustom() {
        if (customFragment == null) {
            customFragment = TrackListStackFragment.newInstance(listener, tracksStorageManager.readCustom());
        }
        return customFragment;
    }

    private TrackListStackFragment getSortByArtist() {
        if (artistFragment == null) {
            artistFragment = TrackListStackFragment.newInstance(listener, tracksStorageManager.readSortedByArtist());
        }
        return artistFragment;
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
