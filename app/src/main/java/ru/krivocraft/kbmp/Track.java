package ru.krivocraft.kbmp;

import android.support.v4.media.MediaMetadataCompat;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ru.krivocraft.kbmp.constants.Constants;

public class Track {

    private boolean selected = false;
    private boolean playing = false;
    private boolean liked = false;
    private boolean checkedInList = false;

    private String title, artist, path;
    private long duration;
    private List<Tag> tags;

    public Track(long duration, String artist, String title, @NonNull String path) {
        this.duration = duration;
        this.artist = artist;
        this.title = title;
        this.path = path;
        this.tags = new ArrayList<>();

        if (artist.equals("<unknown>")) {
            this.artist = Constants.UNKNOWN_ARTIST;
        }
        if (title.equals("<unknown>")) {
            this.title = Constants.UNKNOWN_COMPOSITION;
        }
    }

    MediaMetadataCompat getAsMediaMetadata() {
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

    void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isPlaying() {
        return playing;
    }

    void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public boolean isCheckedInList() {
        return checkedInList;
    }

    void setCheckedInList(boolean checkedInList) {
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

    void setLiked(boolean liked) {
        this.liked = liked;
    }

    void addTag(Tag tag) {
        tags.add(tag);
    }

    void removeTag(Tag tag) {
        tags.remove(tag);
    }

    public List<Tag> getTags() {
        return tags;
    }
}
