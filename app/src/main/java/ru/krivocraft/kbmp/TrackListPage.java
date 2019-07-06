package ru.krivocraft.kbmp;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import java.util.List;

public class TrackListPage extends AbstractTrackViewFragment {

    private List<String> trackList;
    private AdapterView.OnItemClickListener listener;
    private TracksAdapter adapter;
    private ListView listView;
    private boolean showControls;

    public TrackListPage() {
        super();
    }

    @Override
    void invalidate() {
        if (listView != null) {
            listView.invalidateViews();
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    void init(List<String> trackList, AdapterView.OnItemClickListener listener, boolean showControls) {
        this.listener = listener;
        this.showControls = showControls;
        this.trackList = trackList;
    }

    void updateData(List<String> trackList, Context context) {
        if (context != null) {
            this.trackList = trackList;
            this.adapter = new TracksAdapter(trackList, context);
            if (listView != null) {
                this.listView.setAdapter(adapter);
            }
            invalidate();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tracklist, container, false);

        listView = rootView.findViewById(R.id.fragment_track_list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(listener);

        EditText searchFrame = rootView.findViewById(R.id.search_edit_text);
        ImageButton buttonShuffle = rootView.findViewById(R.id.shuffle);

        updateData(trackList, getContext());

        if (showControls) {
            searchFrame.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    Context context = getContext();
                    if (context != null) {
//                        TrackList trackListSearched = Utils.search(s, trackList);
//                        listView.setAdapter(trackListSearched.getTracksAdapter(context));
//                        if (s.length() == 0) {
//                            listView.setAdapter(adapter);
//                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
            buttonShuffle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    trackList.shuffle();
//                    adapter.notifyDataSetChanged();
                }
            });
        } else {
            searchFrame.setVisibility(View.INVISIBLE);
            buttonShuffle.setVisibility(View.INVISIBLE);
        }


        return rootView;
    }
}
