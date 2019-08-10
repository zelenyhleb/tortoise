package ru.krivocraft.kbmp;

import android.support.annotation.NonNull;

import com.google.gson.Gson;

import java.util.List;

class TrackList {
    private String displayName;
    private List<String> tracks;

    TrackList(String displayName, List<String> tracks) {
        this.displayName = displayName;
        this.tracks = tracks;
    }

    @NonNull
    static String createIdentifier(String displayName) {
        return displayName.toLowerCase().replace(" ", "_");
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

}
