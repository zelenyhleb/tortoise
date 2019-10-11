package ru.krivocraft.kbmp.core.track;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import ru.krivocraft.kbmp.R;
import ru.krivocraft.kbmp.core.track.Track;

public class SelectableTracksAdapter extends ArrayAdapter<Track> {

    public SelectableTracksAdapter(List<Track> trackList, Context context) {
        super(context, R.layout.track_list_item_selectable, trackList);
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        Track track = getItem(position);
        View itemView;

        if (convertView != null) {
            itemView = convertView;
        } else {
            itemView = LayoutInflater.from(getContext()).inflate(R.layout.track_list_item_selectable, null);
        }

        if (track != null) {
            ((TextView) itemView.findViewById(R.id.composition_name_text)).setText(track.getTitle());
            ((TextView) itemView.findViewById(R.id.composition_author_text)).setText(track.getArtist());
            ((CheckBox) itemView.findViewById(R.id.composition_checkbox)).setChecked(track.isCheckedInList());
        }

        return itemView;
    }


}
