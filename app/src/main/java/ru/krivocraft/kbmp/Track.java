package ru.krivocraft.kbmp;

import android.support.annotation.NonNull;

import java.io.Serializable;

class Track implements Serializable {

    private String duration;

    private String artist;
    private String name;
    private String path;

    private int identifier;

    Track(@NonNull String duration, String artist, String name, @NonNull String path, int identifier) {

        this.duration = duration;
        this.artist = artist;
        this.name = name;
        this.path = path;
        this.identifier = identifier;

        if (artist == null) {
            this.artist = Constants.UNKNOWN_ARTIST;
        }
        if (name == null) {
            this.name = Constants.UNKNOWN_COMPOSITION;
        }

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

    int getIdentifier() {
        return identifier;
    }

    interface OnTrackFoundListener {
        void onTrackFound(Track track);
    }

    interface OnTrackStateChangedListener {
        void onNewTrackState();
    }
}
