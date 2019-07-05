package ru.krivocraft.kbmp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

class SelectableTracksAdapter extends ArrayAdapter<Track> {

    SelectableTracksAdapter(TrackList trackList, Context context) {
        super(context, R.layout.track_list_item_selectable, trackList.getTracks());
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        Track track = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.track_list_item_selectable, null);
        }
        if (track != null) {
            ((TextView) convertView.findViewById(R.id.composition_name_text)).setText(track.getTitle());
            ((TextView) convertView.findViewById(R.id.composition_author_text)).setText(track.getArtist());
            ((CheckBox) convertView.findViewById(R.id.composition_checkbox)).setChecked(track.isChecked());
        }

        return convertView;
    }
}
