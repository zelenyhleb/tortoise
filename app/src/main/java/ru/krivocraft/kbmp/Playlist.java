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
import android.widget.ImageView;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class Playlist implements Serializable {

    private List<Track> tracks = new ArrayList<>();
    private Context context;
    private TracksAdapter tracksAdapter = null;
    private SelectableTracksAdapter selectableTracksAdapter = null;
    private String name;

    Playlist(Context context) {
        this.context = context;
    }

    Playlist(Context context, String name) {
        this(context);
        this.name = formatName(name);
    }

    Playlist(List<Track> tracks, Context context) {
        this(context);
        this.tracks = tracks;
    }

    private String formatName(String unformatted){
        return unformatted.replaceAll(Constants.PLAYLIST_PREFIX, "").replace("_", " ");
    }

    Context getContext() {
        return context;
    }

    void addComposition(Track track) {
        tracks.add(track);
        notifyAdapters();
    }

    String getName(){
        return name;
    }

    void shuffle() {
        Collections.shuffle(tracks);
    }

    boolean isEmpty() {
        return tracks.isEmpty();
    }

    Track getTrack(int index) {
        if (tracks.size() > 0) {
            return tracks.get(index);
        } else {
            return null;
        }
    }

    TracksAdapter getTracksAdapter() {
        if (tracksAdapter == null) {
            this.tracksAdapter = new TracksAdapter();
        }
        return tracksAdapter;
    }

    SelectableTracksAdapter getSelectableTracksAdapter() {
        if (selectableTracksAdapter == null) {
            this.selectableTracksAdapter = new SelectableTracksAdapter();
        }
        return selectableTracksAdapter;
    }

    private void notifyAdapters() {
        if (tracksAdapter != null) {
            tracksAdapter.notifyDataSetChanged();
        }
        if (selectableTracksAdapter != null) {
            selectableTracksAdapter.notifyDataSetChanged();
        }
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

    class TracksAdapter extends ArrayAdapter<Track> {

        TracksAdapter() {
            super(context, R.layout.composition_list_item, getTracks());
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

                ImageView trackImage = convertView.findViewById(R.id.item_track_image);
                ImageView trackState = convertView.findViewById(R.id.item_track_state);

                if (!track.isSelected()) {
                    trackImage.setImageDrawable(context.getDrawable(R.drawable.ic_track_image_default));
                    trackState.setImageDrawable(null);
                } else {
                    if (track.isPlaying()) {
                        trackState.setImageDrawable(context.getDrawable(R.drawable.ic_pause));
                    } else {
                        trackState.setImageDrawable(context.getDrawable(R.drawable.ic_play));
                    }
                }

                ((TextView) convertView.findViewById(R.id.composition_name_text)).setText(track.getName());
                ((TextView) convertView.findViewById(R.id.composition_author_text)).setText(track.getArtist());
            }

            return convertView;
        }

        Playlist getPlaylist() {
            return Playlist.this;
        }
    }

    class SelectableTracksAdapter extends ArrayAdapter<Track> {

        SelectableTracksAdapter() {
            super(context, R.layout.selectable_composition_list_item, getTracks());
        }

        @SuppressLint("InflateParams")
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            Track track = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.selectable_composition_list_item, null);
            }
            if (track != null) {
                ((TextView) convertView.findViewById(R.id.composition_name_text)).setText(track.getName());
                ((TextView) convertView.findViewById(R.id.composition_author_text)).setText(track.getArtist());
                ((CheckBox) convertView.findViewById(R.id.composition_checkbox)).setChecked(track.isChecked());
            }

            return convertView;
        }
    }

}
