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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

class TrackListsAdapter extends ArrayAdapter<ArrayList<String>> {

    TrackListsAdapter(ArrayList<ArrayList<String>> trackLists, @NonNull Context context) {
        super(context, R.layout.playlists_grid_item, trackLists);
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        ArrayList<String> trackList = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.playlists_grid_item, null);
        }
        if (trackList != null) {
            //TODO: class for track list with name
            ((TextView) convertView.findViewById(R.id.fragment_playlist_name)).setText(Constants.UNKNOWN_ARTIST);
        }

        return convertView;
    }
}
