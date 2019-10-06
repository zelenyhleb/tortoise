package ru.krivocraft.kbmp.api;

import android.content.Context;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

import ru.krivocraft.kbmp.Track;
import ru.krivocraft.kbmp.TrackReference;
import ru.krivocraft.kbmp.sqlite.DBConnection;

public class TracksStorageManager {

    private final DBConnection database;

    public TracksStorageManager(Context context) {
        this.database = new DBConnection(context);
    }

    public List<Track> getTrackStorage() {
        return database.getTracksStorage();
    }

    public void updateTrackStorage(List<Track> tracks) {
        for (int i = 0; i < tracks.size(); i++) {
            updateTrack(tracks.get(i));
        }
    }

    public List<Track> getTracks(List<TrackReference> references) {
        List<Track> tracks = new ArrayList<>();
        for (TrackReference reference : references) {
            tracks.add(getTrack(reference));
        }
        return tracks;
    }

    public void updateTrack(Track updatedTrack) {
        database.updateTrack(updatedTrack);
    }

    public void writeTrack(Track track) {
        database.writeTrack(track);
    }

    public void removeTrack(Track track) {
        database.removeTrack(track);
    }

    public TrackReference getReference(String path) {
        List<Track> trackStorage = getTrackStorage();
        List<String> paths = new ArrayList<>(CollectionUtils.collect(trackStorage, Track::getPath));
        return new TrackReference(trackStorage.get(paths.indexOf(path)));
    }

    List<TrackReference> getReferences(List<Track> tracks) {
        List<TrackReference> references = new ArrayList<>();
        for (Track track : tracks) {
            references.add(getReference(track.getPath()));
        }
        return references;
    }

    public Track getTrack(TrackReference reference) {
        return database.getTrack(reference);
    }
}
