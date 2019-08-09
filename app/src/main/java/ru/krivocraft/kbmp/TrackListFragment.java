package ru.krivocraft.kbmp;

import android.content.Context;
import android.content.Intent;
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
import java.util.Collections;
import java.util.List;

public class TrackListFragment extends Fragment {

    private TrackList trackList;
    private TrackAdapter adapter;
    private ListView listView;
    private boolean showControls;

    public TrackListFragment() {
    }

    static TrackListFragment newInstance(TrackList trackList, boolean showControls, Context context) {
        TrackListFragment trackListFragment = new TrackListFragment();
        trackListFragment.init(showControls, context, trackList);
        return trackListFragment;
    }

    void invalidate() {
        if (listView != null) {
            listView.invalidateViews();
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    TrackList getTrackList() {
        return trackList;
    }

    void init(boolean showControls, Context context, TrackList trackList) {
        if (trackList != null) {
            this.showControls = showControls;
            this.trackList = trackList;
            this.adapter = new TrackAdapter(trackList.getTracks(), context);
        }
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tracklist, container, false);

        listView = rootView.findViewById(R.id.fragment_track_list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Intent serviceIntent = new Intent(Constants.ACTION_PLAY_FROM_LIST);
            serviceIntent.putExtra(Constants.EXTRA_PATH, ((Track) parent.getItemAtPosition(position)).getPath());
            serviceIntent.putExtra(Constants.EXTRA_TRACK_LIST, trackList.getPaths().toArray(new String[0]));
            view.getContext().sendBroadcast(serviceIntent);

            Intent interfaceIntent = new Intent(Constants.ACTION_SHOW_PLAYER);
            view.getContext().sendBroadcast(interfaceIntent);
        });

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
                        List<Track> trackListSearched = Utils.search(s, trackList.getPaths(), context.getContentResolver());
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
                Collections.shuffle(trackList.getTracks());
                adapter.notifyDataSetChanged();
            });
        } else {
            searchFrame.setVisibility(View.INVISIBLE);
            buttonShuffle.setVisibility(View.INVISIBLE);
        }


        return rootView;
    }
}
