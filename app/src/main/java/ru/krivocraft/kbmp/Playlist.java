package ru.krivocraft.kbmp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.io.Serializable;
import java.util.*;

class Playlist implements Serializable {

    private List<Track> tracks = new ArrayList<>();

    void addComposition(Track track) {
        tracks.add(track);
    }

    void removeComposition(Track track) {
        tracks.remove(track);
    }

    void shuffle() {
        Collections.shuffle(tracks);
    }

    Track getComposition(int index) {
        return tracks.get(index);
    }

    void addCompositions(Collection<Track> tracks) {
        this.tracks.addAll(tracks);
    }

    List<Track> getTracks() {
        return tracks;
    }

    int getSize() {
        return tracks.size();
    }

    int indexOf(Track track) {
        return tracks.indexOf(track);
    }

    boolean contains(Track track) {
        return tracks.contains(track);
    }

    boolean contains(String path) {
        for (Track track : tracks) {
            if (track.getPath().equals(path))
                return true;
        }
        return false;
    }

    static class Adapter extends ArrayAdapter<Track> {

        Adapter(Playlist playlist, @NonNull Context context) {
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
}
