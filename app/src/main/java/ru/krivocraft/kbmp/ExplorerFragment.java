package ru.krivocraft.kbmp;

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

import ru.krivocraft.kbmp.api.TrackListsCompiler;
import ru.krivocraft.kbmp.api.TrackListsStorageManager;
import ru.krivocraft.kbmp.constants.Constants;

public class ExplorerFragment extends BaseFragment {

    private TrackListAdapter adapter;
    private OnItemClickListener listener;

    private TrackListsStorageManager trackListsStorageManager;
    private TrackListsCompiler trackListsCompiler;

    private ProgressBar progressBar;

    static ExplorerFragment newInstance(OnItemClickListener listener) {
        ExplorerFragment explorerFragment = new ExplorerFragment();
        explorerFragment.setListener(listener);
        return explorerFragment;
    }

    private void onNewTrackLists(List<TrackList> trackLists) {
        Activity activity = getActivity();
        if (activity != null) {
            for (TrackList trackList : trackLists) {
                if (trackListsStorageManager.getExistingTrackListNames().contains(trackList.getDisplayName())) {
                    trackListsStorageManager.updateTrackList(trackList);
                } else {
                    trackListsStorageManager.writeTrackList(trackList);
                }
            }
            activity.runOnUiThread(ExplorerFragment.this::drawTrackLists);
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            invalidate();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getContext();
        if (context != null) {
            this.trackListsStorageManager = new TrackListsStorageManager(context);
            this.trackListsCompiler = new TrackListsCompiler(context);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_explorer, container, false);
        this.progressBar = rootView.findViewById(R.id.explorer_progress);
        Context context = getContext();
        if (context != null) {
            context.registerReceiver(receiver, new IntentFilter(Constants.Actions.ACTION_UPDATE_STORAGE));
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
        if (!(itemAtPosition.getDisplayName().equals(Constants.STORAGE_TRACKS_DISPLAY_NAME) || itemAtPosition.getDisplayName().equals(Constants.FAVORITES_DISPLAY_NAME))) {
            Intent intent = new Intent(context, TrackListEditorActivity.class);
            intent.putExtra(Constants.Extras.EXTRA_TRACK_LIST, itemAtPosition.toJson());
            intent.putExtra(TrackListEditorActivity.EXTRA_CREATION, false);
            context.startActivity(intent);
        }
        return true;
    }

    private void showCreationDialog(Context context) {
        Intent intent = new Intent(context, TrackListEditorActivity.class);
        intent.putExtra(Constants.Extras.EXTRA_TRACK_LIST, new TrackList("", new ArrayList<>(), Constants.TRACK_LIST_CUSTOM).toJson());
        intent.putExtra(TrackListEditorActivity.EXTRA_CREATION, true);
        context.startActivity(intent);
    }

    private void drawTrackLists() {
        progressBar.setVisibility(View.GONE);
        redrawList(trackListsStorageManager.readTrackLists());
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

    void invalidate() {
        Context context = getContext();
        if (context != null) {
            trackListsCompiler.compileFavorites(this::onNewTrackLists);
            if (settingsManager.getOption(Constants.KEY_SORT_BY_ARTIST, false)) {
                trackListsCompiler.compileByAuthors(this::onNewTrackLists);
            }
            if (settingsManager.getOption(Constants.KEY_SORT_BY_TAG, false)) {
                trackListsCompiler.compileByTags(this::onNewTrackLists);
            }
        }
    }

    private void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    interface OnItemClickListener {
        void onItemClick(TrackList trackList);
    }

}
