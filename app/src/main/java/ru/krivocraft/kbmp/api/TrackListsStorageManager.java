package ru.krivocraft.kbmp.api;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.List;

import ru.krivocraft.kbmp.TrackList;
import ru.krivocraft.kbmp.TrackReference;
import ru.krivocraft.kbmp.sqlite.DBConnection;

public class TrackListsStorageManager {

    private DBConnection database;

    public TrackListsStorageManager(@NonNull Context context) {
        this.database = new DBConnection(context);
    }

    public void writeTrackLists(List<TrackList> trackLists) {
        for (TrackList trackList : trackLists) {
            writeTrackList(trackList);
        }
    }

    public void updateTrackList(TrackList trackList) {
        database.updateTrackList(trackList);
    }

    public void updateRootTrackList(TrackList trackList) {
        database.updateRootTrackList(trackList);
    }

    public void removeTracks(TrackList trackList, List<TrackReference> references) {
        database.removeTracks(trackList, references);
    }

    public void writeTrackList(TrackList trackList) {
        database.writeTrackList(trackList);
    }

    public void removeTrackList(TrackList trackList) {
        database.removeTrackList(trackList);
    }

    public List<TrackList> readTrackLists() {
        return database.getTrackLists();
    }

    public List<String> getUnavailableTrackListNames() {
        return database.getTrackListNames();
    }

}
