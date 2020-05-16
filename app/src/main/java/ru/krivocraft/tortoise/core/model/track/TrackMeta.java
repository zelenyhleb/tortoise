package ru.krivocraft.tortoise.core.model.track;

public class TrackMeta {

    private final String title;
    private final String artist;
    private final String path;
    private final long duration;
    private final int color;

    public TrackMeta(String title, String artist, String path, long duration, int color) {
        this.title = title;
        this.artist = artist;
        this.path = path;
        this.duration = duration;
        this.color = color;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getPath() {
        return path;
    }

    public long getDuration() {
        return duration;
    }

    public int getColor() {
        return color;
    }
}
