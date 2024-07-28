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

package ru.krivocraft.tortoise.android.explorer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import ru.krivocraft.tortoise.R;
import ru.krivocraft.tortoise.android.BaseFragment;
import ru.krivocraft.tortoise.android.editors.TrackListEditorActivity;
import ru.krivocraft.tortoise.android.player.SharedPreferencesSettings;
import ru.krivocraft.tortoise.core.model.Track;
import ru.krivocraft.tortoise.core.model.TrackList;
import ru.krivocraft.tortoise.android.player.MediaService;
import ru.krivocraft.tortoise.core.rating.Shuffle;
import ru.krivocraft.tortoise.android.settings.SettingsStorageManager;
import ru.krivocraft.tortoise.android.tracklist.TracksStorageManager;

import java.util.ArrayList;
import java.util.Random;

public class ExplorerFragment extends BaseFragment {

    private ExplorerPagerAdapter adapter;

    private Explorer explorer;

    private TrackListsGridFragment.OnItemClickListener listener;
    private FloatingActionButton addButton;
    private FloatingActionButton playButton;
    private int tintColor;
    private TabLayout tabLayout;

    public static ExplorerFragment newInstance() {
        return new ExplorerFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_explorer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        addButton = view.findViewById(R.id.add_track_list_button);
        addButton.setOnClickListener(v -> showCreationDialog(v.getContext()));

        playButton = view.findViewById(R.id.play_all_button);
        playButton.setOnClickListener(v -> playAll(v.getContext()));

        tabLayout = view.findViewById(R.id.explorer_tabs);

        if (tintColor != 0) {
            if (addButton != null) {
                addButton.setBackgroundTintList(getContext().getResources().getColorStateList(tintColor));
            }
            if (playButton != null) {
                playButton.setBackgroundTintList(getContext().getResources().getColorStateList(tintColor));
            }
            if (tabLayout != null) {
                tabLayout.setSelectedTabIndicatorColor(getContext().getResources().getColor(tintColor));
            }
        }

        ViewPager pager = view.findViewById(R.id.explorer_pager);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //Do nothing
            }

            @Override
            public void onPageSelected(int position) {
                getSettingsManager().put("endOnSorted", position > 0);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) playButton.getLayoutParams();
                if (position > 0) {
                    addButton.hide();
                    params.addRule(RelativeLayout.ALIGN_PARENT_END);
                } else {
                    addButton.show();
                    params.removeRule(RelativeLayout.ALIGN_PARENT_END);
                }
                playButton.setLayoutParams(params);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                //Do nothing
            }
        });

        Context activity = view.getContext();
        if (activity != null && explorer != null) {
            this.adapter = new ExplorerPagerAdapter(getChildFragmentManager(), listener, explorer.getCustomLists(), explorer.getSortedLists(), activity);
            pager.setAdapter(adapter);
            pager.setCurrentItem(getSettingsManager().get("endOnSorted", false) ? 1 : 0);
            tabLayout.setupWithViewPager(pager);
        }
    }

    @Override
    public void invalidate() {
        this.adapter.setTrackLists(explorer.getCustomLists(), explorer.getSortedLists());
    }

    @Override
    public void changeColors(int color) {
        Context context = getContext();
        if (context != null) {
            if (addButton != null) {
                addButton.setBackgroundTintList(context.getResources().getColorStateList(color));
            }
            if (playButton != null) {
                playButton.setBackgroundTintList(getContext().getResources().getColorStateList(color));
            }
            if (tabLayout != null) {
                tabLayout.setSelectedTabIndicatorColor(context.getResources().getColor(color));
            }
        }
    }

    private void showCreationDialog(Context context) {
        Intent intent = new Intent(context, TrackListEditorActivity.class);
        intent.putExtra(TrackList.EXTRA_TRACK_LIST, new TrackList("", new ArrayList<>(), TrackList.TRACK_LIST_CUSTOM).toJson());
        intent.putExtra(TrackListEditorActivity.EXTRA_CREATION, true);
        context.startActivity(intent);
    }

    private void playAll(Context context) {
        TrackList trackList = new TrackListsStorageManager(context, TrackListsStorageManager.FILTER_ALL).getAllTracks();
        if (trackList.size() > 0) {
            Track.Reference reference;
            if (trackList.size() > 1) {
                int randomTrack = new Random().nextInt(trackList.size() - 1);
                reference = trackList.getTrackReferences().get(randomTrack);
                trackList.shuffle(new Shuffle(new TracksStorageManager(context), new SharedPreferencesSettings(context)), reference);
            } else {
                reference = trackList.get(0);
            }
            Intent serviceIntent = new Intent(MediaService.ACTION_PLAY_FROM_LIST);
            serviceIntent.putExtra(Track.EXTRA_TRACK, reference.toJson());
            serviceIntent.putExtra(TrackList.EXTRA_TRACK_LIST, trackList.toJson());
            context.sendBroadcast(serviceIntent);
        }
    }

    public void setListener(TrackListsGridFragment.OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setExplorer(Explorer explorer) {
        this.explorer = explorer;
    }

    public void setTintColor(int tintColor) {
        this.tintColor = tintColor;
    }
}
