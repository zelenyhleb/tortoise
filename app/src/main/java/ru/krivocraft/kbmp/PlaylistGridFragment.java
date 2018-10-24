package ru.krivocraft.kbmp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

public class PlaylistGridFragment extends Fragment {

    private Playlist.PlaylistsAdapter adapter;
    private AdapterView.OnItemClickListener listener;

    public PlaylistGridFragment() {
        //required empty public constructor
    }

    void setData(Playlist.PlaylistsAdapter adapter, AdapterView.OnItemClickListener listener) {
        this.adapter = adapter;
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_playlistgrid, container, false);
        GridView gridView = rootView.findViewById(R.id.playlists_grid);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(listener);
        return rootView;
    }
}
