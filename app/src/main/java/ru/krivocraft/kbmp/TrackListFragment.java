package ru.krivocraft.kbmp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.Collections;
import java.util.List;

import ru.krivocraft.kbmp.constants.Constants;

public class TrackListFragment extends Fragment {

    private TrackAdapter adapter;
    private boolean showControls;

    private TrackList trackList;
    private List<Track> tracks;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            TrackList trackList = TrackList.fromJson(intent.getStringExtra(Constants.Extras.EXTRA_TRACK_LIST));
            if (trackList != null) {
                TrackListFragment.this.trackList = trackList;
            }
            processPaths(context);
        }
    };
    private ListView listView;
    private ProgressBar progressBar;

    public TrackListFragment() {
    }

    static TrackListFragment newInstance(TrackList trackList, boolean showControls) {
        TrackListFragment trackListFragment = new TrackListFragment();
        trackListFragment.init(showControls, trackList);
        return trackListFragment;
    }

    private void init(boolean showControls, TrackList trackList) {
        this.showControls = showControls;
        this.trackList = trackList;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tracklist, container, false);

        EditText searchFrame = rootView.findViewById(R.id.search_edit_text);
        ImageButton buttonShuffle = rootView.findViewById(R.id.shuffle);
        listView = rootView.findViewById(R.id.fragment_track_list);
        progressBar = rootView.findViewById(R.id.track_list_progress);

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
                        List<Track> trackListSearched = Utils.search(s, TrackListFragment.this.trackList.getTracks(), context.getContentResolver());
                        listView.setAdapter(new TrackAdapter(trackListSearched, context));
                        if (s.length() == 0) {
                            listView.setAdapter(adapter);
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });
                buttonShuffle.setOnClickListener(v -> {
                    Collections.shuffle(this.tracks);
                    adapter.notifyDataSetChanged();
                });
                searchFrame.setVisibility(View.VISIBLE);
                buttonShuffle.setVisibility(View.VISIBLE);
            } else {
                IntentFilter filter = new IntentFilter();
                filter.addAction(Constants.Actions.ACTION_UPDATE_TRACK_LIST);
                context.registerReceiver(receiver, filter);
            }
        }


        return rootView;
    }

    private void processPaths(Context context) {
        LoadDataTask task = new LoadDataTask();
        task.setContentResolver(context.getContentResolver());
        task.setCallback(tracks -> {
            this.tracks = tracks;
            this.adapter = new TrackAdapter(this.tracks, context);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener((parent, view, position, id) -> onItemClick(trackList, parent, view, position));

            progressBar.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
        });
        task.execute(trackList.getTracks().toArray(new String[0]));
    }

    @Override
    public void onDestroy() {
        Context context = getContext();
        if (context != null) {
            context.unregisterReceiver(receiver);
        }
        super.onDestroy();
    }

    private void onItemClick(TrackList trackList, AdapterView<?> parent, View view, int position) {
        Intent serviceIntent = new Intent(Constants.Actions.ACTION_PLAY_FROM_LIST);
        serviceIntent.putExtra(Constants.Extras.EXTRA_PATH, ((Track) parent.getItemAtPosition(position)).getPath());
        serviceIntent.putExtra(Constants.Extras.EXTRA_TRACK_LIST, trackList.toJson());
        view.getContext().sendBroadcast(serviceIntent);

        Intent interfaceIntent = new Intent(Constants.Actions.ACTION_SHOW_PLAYER);
        view.getContext().sendBroadcast(interfaceIntent);
    }
}
