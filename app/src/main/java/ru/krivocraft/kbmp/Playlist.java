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

    private String name;
    private String image;

    private List<Track> tracks = new ArrayList<>();

    Playlist() {

    }

    Playlist(String name) {

    }

    void addComposition(Track track) {
        tracks.add(track);
    }

    void shuffle() {
        Collections.shuffle(tracks);
    }

    boolean isEmpty() {
        return tracks.isEmpty();
    }

    Track getComposition(int index) {
        return tracks.get(index);
    }

    private List<Track> getTracks() {
        return tracks;
    }

    int getSize() {
        return tracks.size();
    }

    Playlist search(CharSequence string){
        Playlist playlist = new Playlist();
        for (Track track : tracks) {
            if (track.getName().contains(string) || track.getArtist().contains(string)){
                playlist.addComposition(track);
            }
        }
        return playlist;
    }

    int indexOf(Track track) {
        return tracks.indexOf(track);
    }

    boolean contains(Track track) {
        return tracks.contains(track);
    }

    public String getName() {
        return name;
    }

    static class TracksAdapter extends ArrayAdapter<Track> {

        private Playlist playlist;

        TracksAdapter(Playlist playlist, @NonNull Context context) {
            super(context, R.layout.composition_list_item, playlist.getTracks());
            this.playlist = playlist;
        }

        public void setPlaylist(Playlist playlist) {
            this.playlist = playlist;
            notifyDataSetChanged();
        }

        @SuppressLint("InflateParams")
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            Track track = getItem(position);

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

    static class PlaylistsAdapter extends ArrayAdapter<Playlist> {

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
                ((TextView) convertView.findViewById(R.id.fragment_playlist_name)).setText(playlist.getName());
            }

            return convertView;
        }
    }
}
