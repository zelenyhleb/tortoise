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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TrackListFragment extends Fragment {

    private List<String> trackList;
    private TrackAdapter adapter;
    private ListView listView;
    private boolean showControls;
    private boolean startedWithList;
    private ProgressBar progressBar;

    public TrackListFragment() {
    }

    static TrackListFragment newInstance(TrackList trackList, boolean showControls, Context context) {
        TrackListFragment trackListFragment = new TrackListFragment();
        trackListFragment.init(showControls, context, trackList);
        return trackListFragment;
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateData(context, intent);
        }
    };

    private void updateData(final Context context, Intent intent) {
        TrackListFragment.this.trackList = Arrays.asList(intent.getStringArrayExtra(Constants.EXTRA_TRACK_LIST));

        LoadDataTask loadDataTask = new LoadDataTask();
        loadDataTask.setContentResolver(context.getContentResolver());
        loadDataTask.setCallback(track -> {
            TrackListFragment.this.adapter = new TrackAdapter(track, context);
            if (listView != null) {
                TrackListFragment.this.listView.setAdapter(adapter);
            }
            progressBar.setVisibility(View.INVISIBLE);
            listView.setVisibility(View.VISIBLE);
            invalidate();
        });
        loadDataTask.execute(trackList.toArray(new String[0]));
    }

    void invalidate() {
        if (listView != null) {
            listView.invalidateViews();
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    List<String> getTrackList() {
        return trackList;
    }

    void init(boolean showControls, Context context, TrackList trackList) {
        this.showControls = showControls;

        if (trackList == null) {
            this.trackList = new ArrayList<>();
            this.adapter = new TrackAdapter(new ArrayList<>(), context);
            startedWithList = false;
        } else {
            this.trackList = trackList.getPaths();
            this.adapter = new TrackAdapter(trackList.getTracks(), context);
            startedWithList = true;
        }
        registerUpdateReceiver(context);
    }

    private void registerUpdateReceiver(Context context) {
        IntentFilter filter = new IntentFilter(Constants.ACTION_UPDATE_TRACK_LIST);
        context.registerReceiver(receiver, filter);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tracklist, container, false);

        progressBar = rootView.findViewById(R.id.track_list_progress_bar);
        progressBar.setVisibility(View.INVISIBLE);

        listView = rootView.findViewById(R.id.fragment_track_list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Intent serviceIntent = new Intent(Constants.ACTION_PLAY_FROM_LIST);
            serviceIntent.putExtra(Constants.EXTRA_PATH, ((Track) parent.getItemAtPosition(position)).getPath());
            serviceIntent.putExtra(Constants.EXTRA_TRACK_LIST, getTrackList().toArray(new String[0]));
            view.getContext().sendBroadcast(serviceIntent);

            Intent interfaceIntent = new Intent(Constants.ACTION_SHOW_PLAYER);
            view.getContext().sendBroadcast(interfaceIntent);
        });

        if (!startedWithList) {
            progressBar.setVisibility(View.VISIBLE);
            listView.setVisibility(View.INVISIBLE);
        }

        EditText searchFrame = rootView.findViewById(R.id.search_edit_text);
        ImageButton buttonShuffle = rootView.findViewById(R.id.shuffle);

        if (showControls) {
            searchFrame.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    Context context = getContext();
                    if (context != null) {
                        List<Track> trackListSearched = Utils.search(s, trackList, context.getContentResolver());
                        listView.setAdapter(new TrackAdapter(trackListSearched, context));
                        if (s.length() == 0) {
                            listView.setAdapter(adapter);
                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
            buttonShuffle.setOnClickListener(v -> {
                Context context = getContext();
                if (context != null) {
                    context.sendBroadcast(new Intent(Constants.ACTION_SHUFFLE));
                }
            });
        } else {
            searchFrame.setVisibility(View.INVISIBLE);
            buttonShuffle.setVisibility(View.INVISIBLE);
        }


        return rootView;
    }
}
