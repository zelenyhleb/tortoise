package ru.krivocraft.kbmp;

import android.content.Context;
import android.os.AsyncTask;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class TrackList implements Serializable {

    private List<Track> tracks = new ArrayList<>();

    private TracksAdapter tracksAdapter = null;
    private SelectableTracksAdapter selectableTracksAdapter = null;

    private String name;
    private boolean selected;
    private int cursor;


    TrackList(String name) {
        if (name.contains(Constants.PLAYLIST_PREFIX)) {
            this.name = formatName(name);
        } else {
            this.name = name;
        }
        cursor = -1;
    }

    TrackList(List<Track> tracks, String name) {
        this(name);
        this.tracks = tracks;
    }

    private String formatName(String unformatted) {
        return unformatted.replaceAll(Constants.PLAYLIST_PREFIX, "").replace("_", " ");
    }

    void setCursor(int newCursor) {
        cursor = newCursor;
    }

    int getCursor() {
        return cursor;
    }

    Track getSelectedTrack() {
        if (cursor < getSize() && cursor >= 0)
            return tracks.get(cursor);
        else
            return null;
    }

    void addTrack(Track track) {
        tracks.add(track);
    }

    void addTracks(List<Track> tracks) {
        this.tracks.addAll(tracks);
    }

    String getName() {
        return name;
    }

    void deselect() {
        for (Track track : tracks) {
            track.setPlaying(false);
            track.setSelected(false);
        }
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

    TracksAdapter getTracksAdapter(Context context) {
        if (tracksAdapter == null) {
            this.tracksAdapter = new TracksAdapter(this, context);
        }
        return tracksAdapter;
    }

    SelectableTracksAdapter getSelectableTracksAdapter(Context context) {
        if (selectableTracksAdapter == null) {
            this.selectableTracksAdapter = new SelectableTracksAdapter(this, context);
        }
        return selectableTracksAdapter;
    }

    void notifyAdapters() {
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

    interface OnPlaylistCompilingCompleted {
        void onPlaylistCompiled(List<TrackList> list);
    }

    static class CompilePlaylistsTask extends AsyncTask<TrackList, Void, List<TrackList>> {

        OnPlaylistCompilingCompleted listener;

        @Override
        protected List<TrackList> doInBackground(TrackList... trackLists) {
            return Utils.compilePlaylistsByAuthor(trackLists[0]);
        }

        @Override
        protected void onPostExecute(List<TrackList> trackLists) {
            listener.onPlaylistCompiled(trackLists);
        }
    }

}
