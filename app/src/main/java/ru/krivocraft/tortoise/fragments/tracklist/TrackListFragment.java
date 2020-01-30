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

package ru.krivocraft.tortoise.fragments.tracklist;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ru.krivocraft.tortoise.R;
import ru.krivocraft.tortoise.core.ItemTouchHelperCallback;
import ru.krivocraft.tortoise.core.Searcher;
import ru.krivocraft.tortoise.core.playback.MediaService;
import ru.krivocraft.tortoise.core.storage.TracksStorageManager;
import ru.krivocraft.tortoise.core.track.TrackList;
import ru.krivocraft.tortoise.core.track.TrackReference;
import ru.krivocraft.tortoise.core.track.TracksAdapter;
import ru.krivocraft.tortoise.fragments.BaseFragment;

public class TrackListFragment extends BaseFragment {

    private TracksAdapter tracksAdapter;
    private ItemTouchHelper touchHelper;
    private boolean showControls;

    private TrackList trackList;

    private BroadcastReceiver trackListReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            TrackList trackList = TrackList.fromJson(intent.getStringExtra(TrackList.EXTRA_TRACK_LIST));
            if (trackList != null) {
                setTrackList(trackList);
            }
        }
    };

    private RecyclerView recyclerView;
    private ProgressBar progressBar;

    private TracksStorageManager tracksStorageManager;

    private MediaControllerCompat.Callback callback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            if (tracksAdapter != null) {
                tracksAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            if (tracksAdapter != null) {
                tracksAdapter.notifyDataSetChanged();
            }
        }
    };
    private MediaControllerCompat mediaController;

    public static TrackListFragment newInstance(boolean showControls, Activity context, MediaControllerCompat mediaController) {
        TrackListFragment trackListFragment = new TrackListFragment();
        trackListFragment.init(showControls, context, mediaController);
        return trackListFragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            if (!showControls) {
                IntentFilter filter = new IntentFilter();
                filter.addAction(MediaService.ACTION_UPDATE_TRACK_LIST);
                context.registerReceiver(trackListReceiver, filter);
            }
        }

    }

    private void init(boolean showControls, Activity context, MediaControllerCompat mediaController) {
        this.mediaController = mediaController;
        this.mediaController.registerCallback(callback);
        this.showControls = showControls;
        this.tracksStorageManager = new TracksStorageManager(context);
    }

    @Override
    public void invalidate() {
        if (tracksAdapter != null) {
            tracksAdapter.notifyDataSetChanged();
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
        new Thread(() -> {
            EditText searchFrame = view.findViewById(R.id.search_edit_text);
            recyclerView = view.findViewById(R.id.fragment_track_recycler_view);
            progressBar = view.findViewById(R.id.track_list_progress);

            final Activity context = getActivity();
            if (context != null) {
                processPaths(context, trackList);

                if (showControls) {
                    searchFrame.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                            //Do nothing
                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            Searcher searcher = new Searcher(context);
                            List<TrackReference> trackListSearched = searcher.search(s, TrackListFragment.this.trackList.getTrackReferences());

                            recyclerView.setAdapter(new TracksAdapter(
                                    new TrackList("found", trackListSearched, TrackList.TRACK_LIST_CUSTOM),
                                    context,
                                    showControls,
                                    true,
                                    null
                            ));
                            if (s.length() == 0) {
                                recyclerView.setAdapter(tracksAdapter);
                            }
                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            //Do nothing
                        }
                    });
                }

                context.runOnUiThread(() -> {
                    if (showControls) {
                        searchFrame.setVisibility(View.VISIBLE);
                    } else {
                        searchFrame.setHeight(0);
                    }
                    touchHelper.attachToRecyclerView(recyclerView);
                    progressBar.setVisibility(View.GONE);
                });
            }
        }).start();

    }

    private synchronized void processPaths(Activity context, TrackList trackList) {
        if (this.touchHelper != null) {
            this.touchHelper.attachToRecyclerView(null);
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        this.tracksAdapter = new TracksAdapter(trackList, context, showControls, !showControls, (from, to) -> {
            // Some ancient magic below
            int firstPos = layoutManager.findFirstCompletelyVisibleItemPosition();
            int offsetTop = 0;

            if (firstPos >= 0) {
                View firstView = layoutManager.findViewByPosition(firstPos);
                offsetTop = layoutManager.getDecoratedTop(firstView) - layoutManager.getTopDecorationHeight(firstView);
            }

            tracksAdapter.notifyItemMoved(from, to);

            if (firstPos >= 0) {
                layoutManager.scrollToPositionWithOffset(firstPos, offsetTop);
            }
        });

        context.runOnUiThread(() -> {
            this.recyclerView.setLayoutManager(layoutManager);
            this.recyclerView.setAdapter(tracksAdapter);
            if (!showControls) {
                layoutManager.scrollToPosition(getSelectedItem());
            }
            ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(tracksAdapter);
            touchHelper = new ItemTouchHelper(callback);
        });

    }

    private int getSelectedItem() {
        if (trackList != null) {
            for (TrackReference reference : trackList.getTrackReferences()) {
                if (tracksStorageManager.getTrack(reference).isSelected()) {
                    return trackList.getTrackReferences().indexOf(reference);
                }
            }
        }
        return 0;
    }

    public void setTrackList(TrackList trackList) {
        this.trackList = trackList;
        if (tracksAdapter != null) {
            processPaths(getActivity(), trackList);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!showControls) {
            Context context = getContext();
            if (context != null) {
                try {
                    context.unregisterReceiver(trackListReceiver);
                } catch (IllegalArgumentException e) {
                    //Do nothing
                }
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mediaController != null) {
            mediaController.unregisterCallback(callback);
        }
    }
}
