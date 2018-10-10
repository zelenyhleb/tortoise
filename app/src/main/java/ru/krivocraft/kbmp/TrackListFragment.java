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

    private Playlist.Adapter adapter;
    private AdapterView.OnItemClickListener listener;

    public TrackListFragment() {
        //required empty public constructor
    }

    void setData(Playlist.Adapter adapter, AdapterView.OnItemClickListener listener){
        this.listener = listener;
        this.adapter = adapter;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tracklist, container, false);
        ListView view = rootView.findViewById(R.id.fragment_track_list);
        view.setAdapter(adapter);
        view.setOnItemClickListener(listener);
        return rootView;
    }
}
