package ru.krivocraft.kbmp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

public class PlaylistGridFragment extends AbstractTrackViewFragment {

    private PlaylistsAdapter adapter;
    private AdapterView.OnItemClickListener listener;
    private AdapterView.OnItemLongClickListener longClickListener;
    private GridView gridView;

    public PlaylistGridFragment() {
        super();
    }

    @Override
    void invalidate() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        if (gridView != null) {
            gridView.invalidateViews();
        }
    }

    void setData(PlaylistsAdapter adapter, AdapterView.OnItemClickListener listener, AdapterView.OnItemLongClickListener onGridItemLongClickListener) {
        this.adapter = adapter;
        this.longClickListener = onGridItemLongClickListener;
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_playlistgrid, container, false);
        gridView = rootView.findViewById(R.id.playlists_grid);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(listener);
        gridView.setOnItemLongClickListener(longClickListener);
        return rootView;
    }

    @Override
    public void onTrackStateChanged(Track.TrackState state) {

    }
}
