package ru.krivocraft.kbmp;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

class TrackListAdapter extends ArrayAdapter<TrackList> {

    TrackListAdapter(List<TrackList> trackLists, @NonNull Context context) {
        super(context, R.layout.playlists_grid_item, trackLists);
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TrackList trackList = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.playlists_grid_item, null);
        }
        if (trackList != null) {
            ((TextView) convertView.findViewById(R.id.fragment_playlist_name)).setText(trackList.getDisplayName());
        }
        convertView.findViewById(R.id.fragment_playlist_picture).setClipToOutline(true);
        return convertView;
    }
}
