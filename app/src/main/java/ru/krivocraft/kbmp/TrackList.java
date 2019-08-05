package ru.krivocraft.kbmp;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

class TrackList {
    private String identifier;
    private String displayName;
    private List<Track> tracks;

    TrackList(String identifier, String displayName, List<Track> tracks) {
        this.identifier = identifier;
        this.displayName = displayName;
        this.tracks = tracks;
    }

    String getIdentifier() {
        return identifier;
    }

    List<Track> getTracks() {
        return tracks;
    }

    List<String> getPaths(){
        return new ArrayList<>(CollectionUtils.collect(tracks, Track::getPath));
    }

    String getDisplayName() {
        return displayName;
    }
}
