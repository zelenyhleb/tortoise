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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import ru.krivocraft.tortoise.R;
import ru.krivocraft.tortoise.contexts.TrackListEditorActivity;
import ru.krivocraft.tortoise.core.storage.TrackListsStorageManager;
import ru.krivocraft.tortoise.core.track.TrackList;
import ru.krivocraft.tortoise.core.track.TrackListsAdapter;
import ru.krivocraft.tortoise.core.track.TracksProvider;
import ru.krivocraft.tortoise.fragments.BaseFragment;

import java.util.List;

public class TrackListsGridFragment extends BaseFragment {

    private TrackListsAdapter adapter;
    private OnItemClickListener listener;

    private ProgressBar progressBar;
    private List<TrackList> trackLists;

    public static TrackListsGridFragment newInstance(OnItemClickListener listener, List<TrackList> trackLists, Context context) {
        TrackListsGridFragment fragment = new TrackListsGridFragment();
        fragment.setListener(listener);
        fragment.setTrackLists(trackLists);
        fragment.createAdapter(context);
        return fragment;
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            drawTrackLists();
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_track_list_stack, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.progressBar = view.findViewById(R.id.explorer_progress);
        Context context = getContext();
        if (context != null) {
            configureGridView(view, context);
            context.registerReceiver(receiver, new IntentFilter(TracksProvider.ACTION_UPDATE_STORAGE));
        }
    }

    private void createAdapter(Context context) {
        if (adapter == null) {
            TrackListsAdapter.OnClickListener listener = new TrackListsAdapter.OnClickListener() {
                @Override
                public void onClick(TrackList trackList) {
                    TrackListsGridFragment.this.listener.onItemClick(trackList);
                }

                @Override
                public void onLongClick(TrackList trackList) {
                    showEditor(context, trackList);
                }
            };
            adapter = new TrackListsAdapter(trackLists, context, listener);
        }
    }

    private void configureGridView(View rootView, Context context) {
        RecyclerView gridView = rootView.findViewById(R.id.playlists_grid);
        gridView.setLayoutManager(new GridLayoutManager(context, 2));
        gridView.setAdapter(adapter);
    }

    private boolean showEditor(Context context, TrackList itemAtPosition) {
        if (!(itemAtPosition.getDisplayName().equals(TrackListsStorageManager.STORAGE_TRACKS_DISPLAY_NAME) || itemAtPosition.getDisplayName().equals(TrackListsStorageManager.FAVORITES_DISPLAY_NAME))) {
            Intent intent = new Intent(context, TrackListEditorActivity.class);
            intent.putExtra(TrackList.EXTRA_TRACK_LIST, itemAtPosition.toJson());
            intent.putExtra(TrackListEditorActivity.EXTRA_CREATION, false);
            context.startActivity(intent);
        }
        return true;
    }

    private void drawTrackLists() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
        redrawList(trackLists);
    }

    private void redrawList(List<TrackList> trackLists) {
//        adapter.clear();
//        adapter.addAll(trackLists);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Context context = getContext();
        if (context != null) {
            context.unregisterReceiver(receiver);
        }
    }

    public void invalidate() {
        drawTrackLists();
    }

    private void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setTrackLists(List<TrackList> trackLists) {
        this.trackLists = trackLists;
    }

    public interface OnItemClickListener {
        void onItemClick(TrackList trackList);
    }

}
