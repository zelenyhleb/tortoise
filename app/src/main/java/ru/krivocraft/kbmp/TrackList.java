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

    private List<TrackReference> tracksReferences;

    private List<TrackReference> shuffleCache;

    TrackList(String displayName, List<TrackReference> tracksReferences, boolean custom) {
        this.displayName = displayName;
        this.tracksReferences = tracksReferences;
        this.custom = custom;
    }

    @NonNull
    static String createIdentifier(String displayName) {
        return displayName.toLowerCase().replace(" ", "");
    }

    int shuffle(TrackReference currentTrack) {
        if (!isShuffled()) {
            shuffleCache = new ArrayList<>(tracksReferences);
            tracksReferences.remove(currentTrack);

            Collections.shuffle(tracksReferences);

            List<TrackReference> shuffled = new ArrayList<>();
            shuffled.add(currentTrack);
            shuffled.addAll(tracksReferences);
            tracksReferences = shuffled;
            setShuffled(true);
            return 0;
        } else {
            tracksReferences = new ArrayList<>(shuffleCache);
            shuffleCache = null;
            setShuffled(false);
            return indexOf(currentTrack);
        }

    }

    int indexOf(TrackReference item) {
        return tracksReferences.indexOf(item);
    }

    int size() {
        return tracksReferences.size();
    }

    TrackReference get(int index) {
        return tracksReferences.get(index);
    }

    String getIdentifier() {
        return createIdentifier(displayName);
    }

    List<TrackReference> getTrackReferences() {
        return tracksReferences;
    }

    String getDisplayName() {
        return displayName;
    }

    String toJson() {
        return new Gson().toJson(this);
    }

    void addTrack(TrackReference track) {
        tracksReferences.add(track);
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
