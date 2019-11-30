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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import ru.krivocraft.kbmp.R;
import ru.krivocraft.kbmp.contexts.TrackListEditorActivity;
import ru.krivocraft.kbmp.core.TrackListsCompiler;
import ru.krivocraft.kbmp.core.storage.SettingsStorageManager;
import ru.krivocraft.kbmp.core.storage.TrackListsStorageManager;
import ru.krivocraft.kbmp.core.track.TrackList;
import ru.krivocraft.kbmp.core.track.TrackListAdapter;
import ru.krivocraft.kbmp.core.track.TracksProvider;

public class TrackListStackFragment extends BaseFragment {

    private TrackListAdapter adapter;
    private OnItemClickListener listener;

    private TrackListsStorageManager trackListsStorageManager;
    private TrackListsCompiler trackListsCompiler;

    private ProgressBar progressBar;
    private String filter;

    public static TrackListStackFragment newInstance(OnItemClickListener listener, String filter) {
        TrackListStackFragment explorerFragment = new TrackListStackFragment();
        explorerFragment.setListener(listener);
        explorerFragment.setFilter(filter);
        return explorerFragment;
    }

    private void onNewTrackLists(List<TrackList> trackLists) {
        Activity activity = getActivity();
        if (activity != null) {
            for (TrackList trackList : trackLists) {
                if (trackListsStorageManager.getExistingTrackListNames().contains(trackList.getDisplayName())) {
                    trackListsStorageManager.updateTrackListContent(trackList);
                } else {
                    trackListsStorageManager.writeTrackList(trackList);

                }
            }
            activity.runOnUiThread(TrackListStackFragment.this::drawTrackLists);
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            drawTrackLists();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getContext();
        if (context != null) {
            this.trackListsStorageManager = new TrackListsStorageManager(context, filter);
            this.trackListsCompiler = new TrackListsCompiler(context);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_track_list_stack, container, false);
        this.progressBar = rootView.findViewById(R.id.explorer_progress);
        Context context = getContext();
        if (context != null) {
            context.registerReceiver(receiver, new IntentFilter(TracksProvider.ACTION_UPDATE_STORAGE));
            createAdapter(context);
            configureGridView(rootView, context);
            configureAddButton(rootView, context);
        }
        return rootView;
    }

    private void createAdapter(Context context) {
        if (adapter == null) {
            adapter = new TrackListAdapter(new ArrayList<>(), context);
        }
    }

    private void configureAddButton(View rootView, Context context) {
        FloatingActionButton addTrackList = rootView.findViewById(R.id.add_track_list_button);
        addTrackList.setOnClickListener(v -> showCreationDialog(context));
    }

    private void configureGridView(View rootView, Context context) {
        GridView gridView = rootView.findViewById(R.id.playlists_grid);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener((parent, view, position, id) -> listener.onItemClick((TrackList) parent.getItemAtPosition(position)));
        gridView.setOnItemLongClickListener((parent, view, position, id) -> showEditor(context, parent, position));
    }

    private boolean showEditor(Context context, AdapterView<?> parent, int position) {
        TrackList itemAtPosition = (TrackList) parent.getItemAtPosition(position);
        if (!(itemAtPosition.getDisplayName().equals(TrackListsStorageManager.STORAGE_TRACKS_DISPLAY_NAME) || itemAtPosition.getDisplayName().equals(TrackListsStorageManager.FAVORITES_DISPLAY_NAME))) {
            Intent intent = new Intent(context, TrackListEditorActivity.class);
            intent.putExtra(TrackList.EXTRA_TRACK_LIST, itemAtPosition.toJson());
            intent.putExtra(TrackListEditorActivity.EXTRA_CREATION, false);
            context.startActivity(intent);
        }
        return true;
    }

    private void showCreationDialog(Context context) {
        Intent intent = new Intent(context, TrackListEditorActivity.class);
        intent.putExtra(TrackList.EXTRA_TRACK_LIST, new TrackList("", new ArrayList<>(), TrackList.TRACK_LIST_CUSTOM).toJson());
        intent.putExtra(TrackListEditorActivity.EXTRA_CREATION, true);
        context.startActivity(intent);
    }

    private void drawTrackLists() {
        progressBar.setVisibility(View.GONE);
        redrawList(trackListsStorageManager.readAllTrackLists());
    }

    private void redrawList(List<TrackList> trackLists) {
        adapter.clear();
        adapter.addAll(trackLists);
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
        Context context = getContext();
        if (context != null) {
            trackListsCompiler.compileFavorites(this::onNewTrackLists);
            if (getSettingsManager().getOption(SettingsStorageManager.KEY_SORT_BY_ARTIST, false)) {
                trackListsCompiler.compileByAuthors(this::onNewTrackLists);
            }
        }
    }

    private void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    private void setFilter(String filter) {
        this.filter = filter;
    }

    public interface OnItemClickListener {
        void onItemClick(TrackList trackList);
    }

}
