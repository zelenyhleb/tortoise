package ru.krivocraft.kbmp;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.Serializable;
import java.util.List;

class Track implements Serializable {

    private String duration;

    private String artist;
    private String name;
    private String path;

    private int progress;
    private boolean playing = false;
    private boolean selected = false;
    private boolean checked = false;

    private int identifier;

    enum TrackState {
        NEW_TRACK,
        PLAY_PAUSE_TRACK;
    }

    Track(@NonNull String duration, String artist, String name, @NonNull String path, int identifier) {

        this.duration = duration;
        this.artist = artist;
        this.name = name;
        this.path = path;
        this.identifier = identifier;

        String[] meta = name.split(" - ");
        if (meta.length > 1) {
            this.artist = meta[0];
            this.name = meta[1];
        } else {
            if (artist.equals("<unknown>")) {
                this.artist = Constants.UNKNOWN_ARTIST;
            }
            if (name.equals("<unknown>")) {
                this.name = Constants.UNKNOWN_COMPOSITION;
            }
        }

    }

    boolean isSelected() {
        return selected;
    }

    void setSelected(boolean selected) {
        this.selected = selected;
    }

    int getIdentifier() {
        return identifier;
    }

    boolean isPlaying() {
        return playing;
    }

    void setPlaying(boolean playing) {
        this.playing = playing;
    }

    boolean isChecked() {
        return checked;
    }

    void setChecked(boolean checked) {
        this.checked = checked;
    }

    int getProgress() {
        return progress;
    }

    void setProgress(int progress) {
        this.progress = progress;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Track)) {
            return false;
        }
        Track track = (Track) obj;
        return track.path.equals(path);
    }

    @Override
    public int hashCode() {
        return identifier * 17;
    }

    String getArtist() {
        return artist;
    }

    String getName() {
        return name;
    }

    String getPath() {
        return path;
    }

    String getDuration() {
        return duration;
    }

    static class GetBitmapTask extends AsyncTask<File, Void, Bitmap> {

        private OnPictureProcessedListener listener;

        @Override
        protected Bitmap doInBackground(File... files) {
            return Utils.getTrackBitmap(files[0]);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (listener != null) {
                listener.onPictureProcessed(bitmap);
            }
        }

        void setListener(OnPictureProcessedListener listener) {
            this.listener = listener;
        }
    }

    interface OnPictureProcessedListener {
        void onPictureProcessed(Bitmap bitmap);
    }

    interface OnTracksFoundListener {
        void onTrackSearchingCompleted(List<Track> tracks);
    }

    interface OnTrackStateChangedListener {
        void onTrackStateChanged(TrackState state);
    }
}
