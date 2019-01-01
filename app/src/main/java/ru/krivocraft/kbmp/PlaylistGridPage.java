package ru.krivocraft.kbmp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

public class PlaylistGridPage extends AbstractTrackViewFragment {

    private PlaylistsAdapter adapter;
    private AdapterView.OnItemClickListener listener;
    private AdapterView.OnItemLongClickListener longClickListener;
    private GridView gridView;

    public PlaylistGridPage() {
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

        FloatingActionButton addPlaylistButton = rootView.findViewById(R.id.add_playlist_button);
        addPlaylistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = getContext();
                if (context != null) {
                    Intent intent = new Intent(context, PlaylistCreationActivity.class);
                    startActivity(intent);
                }
            }
        });

        return rootView;
    }

    @Override
    public void onMetadataChanged(MediaMetadataCompat metadata) {

    }

    @Override
    public void onPlaybackStateChanged(PlaybackStateCompat playbackState) {

    }
}
