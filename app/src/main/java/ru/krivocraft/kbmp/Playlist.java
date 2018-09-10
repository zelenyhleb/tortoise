package ru.krivocraft.kbmp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class Playlist implements Serializable {

    private List<Track> tracks = new ArrayList<>();

    void addComposition(Track track) {
        tracks.add(track);
    }

    void removeComposition(Track track) {
        tracks.remove(track);
    }

    void shuffle() {
    }

    Track getComposition(int index) {
        return tracks.get(index);
    }

    void addCompositions(Collection<Track> tracks) {
        this.tracks.addAll(tracks);
    }

    List<Track> getTracks() {
        return tracks;
    }

    int getSize() {
        return tracks.size();
    }

    int indexOf(Track track) {
        return tracks.indexOf(track);
    }

    boolean contains(Track track) {
        return tracks.contains(track);
    }

    boolean contains(String path) {
        for (Track track : tracks) {
            if (track.getPath().equals(path))
                return true;
        }
        return false;
    }

}
