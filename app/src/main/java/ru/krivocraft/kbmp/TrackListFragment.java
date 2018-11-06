package ru.krivocraft.kbmp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

public class TrackListFragment extends Fragment {

    private Playlist.TracksAdapter tracksAdapter;
    private AdapterView.OnItemClickListener listener;

    public TrackListFragment() {
        //required empty public constructor
    }

    void setData(Playlist.TracksAdapter tracksAdapter, AdapterView.OnItemClickListener listener) {
        this.listener = listener;
        this.tracksAdapter = tracksAdapter;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tracklist, container, false);
        ListView view = rootView.findViewById(R.id.fragment_track_list);
        view.setAdapter(tracksAdapter);
        view.setOnItemClickListener(listener);
        return rootView;
    }
}
