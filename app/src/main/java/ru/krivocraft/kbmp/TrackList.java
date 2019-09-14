package ru.krivocraft.kbmp;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class TrackList {
    private String displayName;
    private boolean shuffled = false;
    private final int type;

    private List<TrackReference> tracksReferences;

    private List<TrackReference> shuffleCache;

    public TrackList(String displayName, List<TrackReference> tracksReferences, int type) {
        this.displayName = displayName;
        this.tracksReferences = tracksReferences;
        this.type = type;
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

    public String getIdentifier() {
        return createIdentifier(displayName);
    }

    public List<TrackReference> getTrackReferences() {
        return tracksReferences;
    }

    public void removeAll(Collection<TrackReference> trackReferences) {
        tracksReferences.removeAll(trackReferences);
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    public static TrackList fromJson(String json) {
        return new Gson().fromJson(json, TrackList.class);
    }

    boolean isShuffled() {
        return shuffled;
    }

    private void setShuffled(boolean shuffled) {
        this.shuffled = shuffled;
    }

    public int getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrackList trackList = (TrackList) o;
        return type == trackList.type &&
                displayName.equals(trackList.displayName) &&
                tracksReferences.equals(trackList.tracksReferences);
    }

    @Override
    public int hashCode() {
        return Objects.hash(displayName, type, tracksReferences);
    }

}
