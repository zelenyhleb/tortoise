package ru.krivocraft.kbmp;

import android.support.annotation.NonNull;

import com.google.gson.Gson;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

class TrackList {
    private String identifier;
    private String displayName;
    private List<Track> tracks;

    TrackList(String displayName, List<Track> tracks) {
        this.displayName = displayName;
        this.identifier = createIdentifier(displayName);
        this.tracks = tracks;
    }

    @NonNull
    static String createIdentifier(String displayName) {
        return displayName.toLowerCase().replace(" ", "_");
    }

    String getIdentifier() {
        return identifier;
    }

    List<Track> getTracks() {
        return tracks;
    }

    List<String> getPaths() {
        return new ArrayList<>(CollectionUtils.collect(tracks, Track::getPath));
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

    void addTrack(int index, Track track) {
        tracks.add(index, track);
    }

    void removeTrack(int index) {
        tracks.remove(index);
    }

    void removeTrack(Track track){
        tracks.remove(track);
    }

    static TrackList fromJson(String json) {
        return new Gson().fromJson(json, TrackList.class);
    }

}
