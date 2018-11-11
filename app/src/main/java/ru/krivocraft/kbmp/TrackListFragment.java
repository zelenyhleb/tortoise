package ru.krivocraft.kbmp;

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

public class TrackListFragment extends AbstractTrackViewFragment {

    private Playlist playlist;
    private AdapterView.OnItemClickListener listener;
    private Playlist.TracksAdapter adapter;
    private ListView listView;

    public TrackListFragment() {
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

    void setData(Playlist playlist, AdapterView.OnItemClickListener listener) {
        this.listener = listener;
        this.playlist = playlist;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tracklist, container, false);

        listView = rootView.findViewById(R.id.fragment_track_list);
        adapter = playlist.getTracksAdapter();

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(listener);

        EditText searchFrame = rootView.findViewById(R.id.search_edit_text);
        searchFrame.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Playlist playlistSearched = Utils.search(s, playlist);
                listView.setAdapter(playlistSearched.getTracksAdapter());
                if (s.length() == 0) {
                    listView.setAdapter(adapter);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        ImageButton buttonShuffle = rootView.findViewById(R.id.shuffle);
        buttonShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playlist.shuffle();
                adapter.notifyDataSetChanged();
            }
        });

        return rootView;
    }

    @Override
    public void onTrackStateChanged(Track.TrackState state) {

    }
}
