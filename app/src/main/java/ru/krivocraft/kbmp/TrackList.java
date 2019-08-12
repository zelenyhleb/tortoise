package ru.krivocraft.kbmp;

import android.support.annotation.NonNull;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class TrackList {
    private String displayName;
    private boolean shuffled = false;
    private final boolean custom;

    private List<Track> tracks;

    private List<Track> shuffleCache;

    TrackList(String displayName, List<Track> tracks, boolean custom) {
        this.displayName = displayName;
        this.tracks = tracks;
        this.custom = custom;
    }

    @NonNull
    static String createIdentifier(String displayName) {
        return displayName.toLowerCase().replace(" ", "");
    }

    int shuffle(Track currentTrack) {
        if (!isShuffled()) {
            shuffleCache = new ArrayList<>(tracks);
            tracks.remove(currentTrack);

            Collections.shuffle(tracks);

            List<Track> shuffled = new ArrayList<>();
            shuffled.add(currentTrack);
            shuffled.addAll(tracks);
            tracks = shuffled;
            setShuffled(true);
            return 0;
        } else {
            tracks = new ArrayList<>(shuffleCache);
            shuffleCache = null;
            setShuffled(false);
            return indexOf(currentTrack);
        }

    }

    int indexOf(Track item) {
        return tracks.indexOf(item);
    }

    int size() {
        return tracks.size();
    }

    Track get(int index) {
        return tracks.get(index);
    }

    String getIdentifier() {
        return createIdentifier(displayName);
    }

    List<Track> getTracks() {
        return tracks;
    }

    String getDisplayName() {
        return displayName;
    }

    String toJson() {
        return new Gson().toJson(this);
    }

    void addTrack(Track track) {
        tracks.add(track);
    }

    static TrackList fromJson(String json) {
        return new Gson().fromJson(json, TrackList.class);
    }

    boolean isShuffled() {
        return shuffled;
    }

    private void setShuffled(boolean shuffled) {
        this.shuffled = shuffled;
    }

    boolean isCustom() {
        return custom;
    }
}
