package ru.krivocraft.kbmp;

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
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ru.krivocraft.kbmp.constants.Constants;

public class TrackListFragment extends BaseFragment {

    private TracksAdapter tracksAdapter;
    private boolean showControls;

    private TrackList trackList;

    private BroadcastReceiver trackListReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            TrackList trackList = TrackList.fromJson(intent.getStringExtra(Constants.Extras.EXTRA_TRACK_LIST));
            if (trackList != null) {
                TrackListFragment.this.trackList = trackList;
                processPaths(context);
            }
        }
    };

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView progressText;

    private MediaControllerCompat.Callback callback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            tracksAdapter.notifyDataSetChanged();
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            tracksAdapter.notifyDataSetChanged();
        }
    };
    private ItemTouchHelper touchHelper;

    static TrackListFragment newInstance(TrackList trackList, boolean showControls, Activity context) {
        TrackListFragment trackListFragment = new TrackListFragment();
        trackListFragment.init(showControls, trackList, context);
        return trackListFragment;
    }

    private void init(boolean showControls, TrackList trackList, Activity context) {
        MediaControllerCompat mediaController = MediaControllerCompat.getMediaController(context);
        mediaController.registerCallback(callback);
        this.showControls = showControls;
        this.trackList = trackList;
    }

    @Override
    void invalidate() {
        tracksAdapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_tracklist, container, false);

        EditText searchFrame = rootView.findViewById(R.id.search_edit_text);
        recyclerView = rootView.findViewById(R.id.fragment_track_recycler_view);
        progressBar = rootView.findViewById(R.id.track_list_progress);
        progressText = rootView.findViewById(R.id.obtaining_text_track_list);

        final Context context = getContext();
        if (context != null) {
            processPaths(context);

            if (showControls) {
                searchFrame.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        Searcher searcher = new Searcher(context);
                        List<TrackReference> trackListSearched = searcher.search(s, TrackListFragment.this.trackList.getTrackReferences());

                        recyclerView.setAdapter(new TracksAdapter(new TrackList("found", trackListSearched, Constants.TRACK_LIST_CUSTOM), context, showControls, true));
                        if (s.length() == 0) {
                            recyclerView.setAdapter(tracksAdapter);
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });
                searchFrame.setVisibility(View.VISIBLE);
            } else {
                searchFrame.setHeight(0);
                IntentFilter filter = new IntentFilter();
                filter.addAction(Constants.Actions.ACTION_UPDATE_TRACK_LIST);
                context.registerReceiver(trackListReceiver, filter);
            }
        }

        return rootView;
    }

    private void processPaths(Context context) {
        if (this.touchHelper != null) {
            this.touchHelper.attachToRecyclerView(null);
        }
        this.recyclerView.setLayoutManager(new LinearLayoutManager(context));
        this.tracksAdapter = new TracksAdapter(trackList, context, showControls, !showControls);
        this.recyclerView.setAdapter(tracksAdapter);

        progressBar.setVisibility(View.GONE);
        progressText.setVisibility(View.GONE);

        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(tracksAdapter);
        touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
