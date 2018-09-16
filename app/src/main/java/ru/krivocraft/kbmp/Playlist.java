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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class Playlist implements Serializable {

    private List<OldTrack> tracks = new ArrayList<>();

    void addComposition(OldTrack track) {
        tracks.add(track);
    }

    void shuffle() {
        Collections.shuffle(tracks);
    }

    boolean isEmpty() {
        return tracks.isEmpty();
    }

    OldTrack getComposition(int index) {
        return tracks.get(index);
    }

    private List<OldTrack> getTracks() {
        return tracks;
    }

    int getSize() {
        return tracks.size();
    }

    int indexOf(OldTrack track) {
        return tracks.indexOf(track);
    }

    boolean contains(OldTrack track) {
        return tracks.contains(track);
    }

    static class Adapter extends ArrayAdapter<OldTrack> {

        Adapter(Playlist playlist, @NonNull Context context) {
            super(context, R.layout.composition_list_item, playlist.getTracks());
        }

        @SuppressLint("InflateParams")
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            OldTrack track = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.composition_list_item, null);
            }
            if (track != null) {
                ((TextView) convertView.findViewById(R.id.composition_name_text)).setText(track.getName());
                ((TextView) convertView.findViewById(R.id.composition_author_text)).setText(track.getArtist());
            }

            return convertView;
        }
    }
}
