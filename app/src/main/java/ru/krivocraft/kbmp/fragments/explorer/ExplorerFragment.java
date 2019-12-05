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

package ru.krivocraft.kbmp.fragments.explorer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

import ru.krivocraft.kbmp.R;
import ru.krivocraft.kbmp.contexts.TrackListEditorActivity;
import ru.krivocraft.kbmp.core.track.TrackList;
import ru.krivocraft.kbmp.fragments.BaseFragment;
import ru.krivocraft.kbmp.fragments.tracklist.TrackListsGridFragment;

public class ExplorerFragment extends BaseFragment {

    private ExplorerPagerAdapter adapter;
    private ViewPager pager;
    private Explorer explorer;

    private TrackListsGridFragment.OnItemClickListener listener;

    public static ExplorerFragment newInstance(TrackListsGridFragment.OnItemClickListener listener, Explorer explorer) {
        ExplorerFragment explorerFragment = new ExplorerFragment();
        explorerFragment.setListener(listener);
        explorerFragment.setExplorer(explorer);
        return explorerFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_explorer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        this.adapter = new ExplorerPagerAdapter(getChildFragmentManager(), listener, explorer.getTracksStorageManager(), getContext());
        new Thread(() -> {

            FloatingActionButton button = view.findViewById(R.id.add_track_list_button);

            FragmentActivity activity = getActivity();
            if (activity != null) {
                configureAddButton(view, activity);
            }

            pager = view.findViewById(R.id.explorer_pager);
            pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    //Do nothing
                }

                @Override
                public void onPageSelected(int position) {
                    getSettingsManager().putOption("endOnSorted", position > 0);
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

            TabLayout tabLayout = view.findViewById(R.id.explorer_tabs);
            invalidate();

            if (activity != null) {
                activity.runOnUiThread(() -> {
                    view.findViewById(R.id.explorer_progress).setVisibility(View.GONE);
                    pager.setAdapter(adapter);
                    pager.setCurrentItem(getSettingsManager().getOption("endOnSorted", false) ? 1 : 0);
                    tabLayout.setupWithViewPager(pager);
                });
            }
        }).start();
    }

    @Override
    public void onResume() {
        super.onResume();
        explorer.compileTrackLists();
    }

    @Override
    public void invalidate() {
        adapter.invalidate();
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

    private void setListener(TrackListsGridFragment.OnItemClickListener listener) {
        this.listener = listener;
    }

    private void setExplorer(Explorer explorer) {
        this.explorer = explorer;
    }
}
