package ru.krivocraft.kbmp;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class TrackList implements Parcelable {

    private List<Track> tracks = new ArrayList<>();

    private TracksAdapter tracksAdapter = null;
    private SelectableTracksAdapter selectableTracksAdapter = null;

    private String playlistTitle;
    private int cursor;

    TrackList(String playlistTitle) {
        if (playlistTitle.contains(Constants.PLAYLIST_PREFIX)) {
            this.playlistTitle = formatName(playlistTitle);
        } else {
            this.playlistTitle = playlistTitle;
        }
        cursor = -1;
    }

    TrackList(List<Track> tracks, String playlistTitle) {
        this(playlistTitle);
        this.tracks = tracks;
    }

    protected TrackList(Parcel in) {
        tracks = in.createTypedArrayList(Track.CREATOR);
        playlistTitle = in.readString();
        cursor = in.readInt();
    }

    public static final Creator<TrackList> CREATOR = new Creator<TrackList>() {
        @Override
        public TrackList createFromParcel(Parcel in) {
            return new TrackList(in);
        }

        @Override
        public TrackList[] newArray(int size) {
            return new TrackList[size];
        }
    };

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

    String getPlaylistTitle() {
        return playlistTitle;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(tracks);
        dest.writeString(playlistTitle);
        dest.writeInt(cursor);
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
