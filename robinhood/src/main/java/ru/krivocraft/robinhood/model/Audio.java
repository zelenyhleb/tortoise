package ru.krivocraft.robinhood.model;

public class Audio {
    private final String audioURI;
    private final String title;
    private final String artist;
    private final long duration;

    public Audio(String audioURI, String title, String artist, long duration) {
        this.audioURI = audioURI;
        this.title = title;
        this.artist = artist;
        this.duration = duration;
    }


}
