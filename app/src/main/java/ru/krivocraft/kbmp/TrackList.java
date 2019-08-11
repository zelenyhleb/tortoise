package ru.krivocraft.kbmp;

import android.support.annotation.NonNull;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class TrackList {
    private String displayName;
    private List<String> tracks;
    private boolean shuffled = false;

    private List<String> shuffleCache;

    TrackList(String displayName, List<String> tracks) {
        this.displayName = displayName;
        this.tracks = tracks;
    }

    @NonNull
    static String createIdentifier(String displayName) {
        return displayName.toLowerCase().replace(" ", "");
    }

    int shuffle(String currentTrack) {
        if (!isShuffled()){
            shuffleCache = new ArrayList<>(tracks);
            tracks.remove(currentTrack);

            Collections.shuffle(tracks);

            List<String> shuffled = new ArrayList<>();
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

    int indexOf(String item) {
        return tracks.indexOf(item);
    }

    int size() {
        return tracks.size();
    }

    String getIdentifier() {
        return createIdentifier(displayName);
    }

    List<String> getTracks() {
        return tracks;
    }

    String getDisplayName() {
        return displayName;
    }

    String toJson() {
        return new Gson().toJson(this);
    }

    void addTrack(String track) {
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
}
