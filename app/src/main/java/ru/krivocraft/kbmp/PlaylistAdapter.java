package ru.krivocraft.kbmp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

class PlaylistAdapter extends ArrayAdapter<Track> {

    PlaylistAdapter(Playlist playlist, @NonNull Context context) {
        super(context, R.layout.composition_list_item, playlist.getTracks());
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        Track track = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.composition_list_item, null);
        }

        ((TextView) convertView.findViewById(R.id.composition_name_text)).setText(track.getName());
        ((TextView) convertView.findViewById(R.id.composition_author_text)).setText(track.getArtist());

        return convertView;
    }
}
