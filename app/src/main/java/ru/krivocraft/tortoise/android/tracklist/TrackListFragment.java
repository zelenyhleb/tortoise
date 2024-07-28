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

package ru.krivocraft.tortoise.android.tracklist;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import ru.krivocraft.tortoise.R;
import ru.krivocraft.tortoise.android.BaseFragment;
import ru.krivocraft.tortoise.android.explorer.TrackListsStorageManager;
import ru.krivocraft.tortoise.android.player.SharedPreferencesSettings;
import ru.krivocraft.tortoise.core.model.Track;
import ru.krivocraft.tortoise.core.model.TrackList;
import ru.krivocraft.tortoise.android.player.MediaService;
import ru.krivocraft.tortoise.core.rating.Shuffle;
import ru.krivocraft.tortoise.android.settings.SettingsStorageManager;
import ru.krivocraft.tortoise.core.search.Search;
import ru.krivocraft.tortoise.android.ui.ItemTouchHelperCallback;

import java.util.List;
import java.util.Random;

public class TrackListFragment extends BaseFragment {

    private TracksAdapter tracksAdapter;
    private ItemTouchHelper touchHelper;
    private RecyclerView recyclerView;

    private TracksStorageManager tracksStorageManager;
    private TrackList trackList;

    private boolean showControls;
    private Button playRandomly;
    private int tintColor;

    public static TrackListFragment newInstance() {
        return new TrackListFragment();
    }

    public void notifyTracksStateChanged() {
        if (tracksAdapter != null) {
            tracksAdapter.notifyDataSetChanged();
        }
    }

    public void setShowControls(boolean showControls) {
        this.showControls = showControls;
    }

    @Override
    public void invalidate() {
        if (tracksAdapter != null) {
            tracksAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void changeColors(int color) {
        if (playRandomly != null) {
            playRandomly.setBackgroundTintList(getContext().getResources().getColorStateList(color));
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_tracklist, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EditText searchFrame = view.findViewById(R.id.search_edit_text);
        recyclerView = view.findViewById(R.id.fragment_track_recycler_view);
        playRandomly = view.findViewById(R.id.play_random_button);

        if (showControls && playRandomly != null) {
            playRandomly.setBackgroundTintList(getContext().getResources().getColorStateList(tintColor));
        }

        final Activity context = getActivity();
        if (context != null) {
            this.tracksStorageManager = new TracksStorageManager(context);
            processPaths(context, trackList);

            if (showControls) {
                searchFrame.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        //Do nothing
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        AsyncTask.execute(() -> {
                            Search search = new Search(tracksStorageManager, new TrackListsStorageManager(context, TrackListsStorageManager.FILTER_ALL));
                            List<Track.Reference> trackListSearched = search.search(s, TrackListFragment.this.trackList.getTrackReferences());
                            context.runOnUiThread(() -> {
                                recyclerView.setAdapter(new TracksAdapter(
                                        new TrackList("found", trackListSearched, TrackList.TRACK_LIST_CUSTOM),
                                        context,
                                        showControls,
                                        null
                                ));
                                if (s.length() == 0) {
                                    recyclerView.setAdapter(tracksAdapter);
                                }
                            });
                        });
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        //Do nothing
                    }
                });

                searchFrame.setVisibility(View.VISIBLE);
                playRandomly.setVisibility(View.VISIBLE);
                playRandomly.setOnClickListener(view1 -> {
                    if (trackList.size() > 0) {
                        Track.Reference reference;
                        if (trackList.size() > 1) {
                            int randomTrack = new Random().nextInt(trackList.size() - 1);
                            reference = trackList.getTrackReferences().get(randomTrack);
                            trackList.shuffle(new Shuffle(tracksStorageManager, new SharedPreferencesSettings(context)), reference);
                        } else {
                            reference = trackList.get(0);
                        }
                        Intent serviceIntent = new Intent(MediaService.ACTION_PLAY_FROM_LIST);
                        serviceIntent.putExtra(Track.EXTRA_TRACK, reference.toJson());
                        serviceIntent.putExtra(TrackList.EXTRA_TRACK_LIST, trackList.toJson());
                        view1.getContext().sendBroadcast(serviceIntent);
                    }
                });
            }
        }

    }

    public TrackList getTrackList() {
        return trackList;
    }

    private void processPaths(Activity context, TrackList trackList) {
        if (this.touchHelper != null) {
            this.touchHelper.attachToRecyclerView(null);
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        this.tracksAdapter = new TracksAdapter(trackList, context, showControls, (from, to) -> {
            // Some ancient magic below
            int firstPos = layoutManager.findFirstCompletelyVisibleItemPosition();
            int offsetTop = 0;

            if (firstPos >= 0) {
                View firstView = layoutManager.findViewByPosition(firstPos);
                if (firstView != null) {
                    offsetTop = layoutManager.getDecoratedTop(firstView) - layoutManager.getTopDecorationHeight(firstView);
                }
            }

            tracksAdapter.notifyItemMoved(from, to);

            if (firstPos >= 0) {
                layoutManager.scrollToPositionWithOffset(firstPos, offsetTop);
            }
        });

        this.recyclerView.setLayoutManager(layoutManager);
        this.recyclerView.setAdapter(tracksAdapter);

        //Swipe will be enabled if:
        //It is a temp playlist in large player fragment
        //It is custom playlist in explorer
        boolean isSwipeEnabled = !showControls || trackList.getType() == TrackList.TRACK_LIST_CUSTOM;

        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(tracksAdapter, isSwipeEnabled);
        touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);

        tracksAdapter.notifyDataSetChanged();
        layoutManager.scrollToPosition(getSelectedItem());
    }

    private int getSelectedItem() {
        if (trackList != null) {
            for (Track.Reference reference : trackList.getTrackReferences()) {
                if (tracksStorageManager.getTrack(reference).isSelected()) {
                    return trackList.getTrackReferences().indexOf(reference);
                }
            }
        }
        return 0;
    }

    public void setTrackList(TrackList trackList) {
        this.trackList = trackList;
        FragmentActivity activity = getActivity();
        if (tracksAdapter != null && activity != null) {
            processPaths(activity, trackList);
        }
    }

    public void setTintColor(int tintColor) {
        this.tintColor = tintColor;
    }
}
