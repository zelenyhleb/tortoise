package ru.krivocraft.kbmp.core.track;

import android.support.v4.media.MediaMetadataCompat;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import java.util.Objects;

public class Track {

    public static final String EXTRA_TRACK = "path";

    private static final String UNKNOWN_ARTIST = "Unknown Artist";
    private static final String UNKNOWN_COMPOSITION = "Unknown Track";

    private boolean selected = false;
    private boolean playing = false;
    private boolean liked = false;
    private boolean checkedInList = false;

    private String title, artist, path;
    private long duration;
    private int identifier;
    private int color;

    public Track(long duration, String artist, String title, @NonNull String path, int color) {
        this.duration = duration;
        this.artist = artist;
        this.title = title;
        this.path = path;
        this.color = color;

        this.identifier = path.hashCode();

        if (artist.equals("<unknown>")) {
            this.artist = UNKNOWN_ARTIST;
        }
        if (title.equals("<unknown>")) {
            this.title = UNKNOWN_COMPOSITION;
        }
    }

    public Track(long duration, String artist, String title, String path, boolean liked, boolean selected, boolean playing, int color) {
        this(duration, artist, title, path, color);
        this.liked = liked;
        this.selected = selected;
        this.playing = playing;
    }

    public MediaMetadataCompat getAsMediaMetadata() {
        return new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, path)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
                .build();
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public boolean isCheckedInList() {
        return checkedInList;
    }

    public void setCheckedInList(boolean checkedInList) {
        this.checkedInList = checkedInList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Track track = (Track) o;
        return Objects.equals(getArtist(), track.getArtist()) &&
                Objects.equals(getTitle(), track.getTitle()) &&
                Objects.equals(getPath(), track.getPath());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getArtist(), getTitle(), getPath());
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    public static Track fromJson(String json) {
        return new Gson().fromJson(json, Track.class);
    }

    public String getArtist() {
        return artist;
    }

    public String getTitle() {
        return title;
    }

    public String getPath() {
        return path;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public int getIdentifier() {
        return identifier;
    }

    public long getDuration() {
        return duration;
    }

    public int getColor() {
        return color;
    }
}
