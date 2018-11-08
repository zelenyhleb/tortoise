package ru.krivocraft.kbmp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

class PlaylistsAdapter extends ArrayAdapter<Playlist> {

    PlaylistsAdapter(List<Playlist> playlists, @NonNull Context context) {
        super(context, R.layout.playlists_grid_item, playlists);
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        Playlist playlist = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.playlists_grid_item, null);
        }
        if (playlist != null) {
            ((TextView) convertView.findViewById(R.id.fragment_playlist_name)).setText(R.string.unknown);
        }

        return convertView;
    }
}
