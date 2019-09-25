package ru.krivocraft.kbmp.api;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import java.util.List;

import ru.krivocraft.kbmp.TrackList;
import ru.krivocraft.kbmp.constants.Constants;
import ru.krivocraft.kbmp.sqlite.DBConnection;
import ru.krivocraft.kbmp.tasks.OnTrackListsReadCallback;
import ru.krivocraft.kbmp.tasks.ReadTrackListsTask;

public class TrackListsStorageManager {

    private SharedPreferences settings;
    private DBConnection database;

    public TrackListsStorageManager(@NonNull Context context) {
        this.database = new DBConnection(context);
        this.settings = context.getSharedPreferences(Constants.STORAGE_SETTINGS, Context.MODE_MULTI_PROCESS);
    }

    public void writeTrackLists(List<TrackList> trackLists) {
        for (TrackList trackList : trackLists) {
            writeTrackList(trackList);
        }
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

    public void readTrackListsAsync(OnTrackListsReadCallback callback) {
        ReadTrackListsTask task = new ReadTrackListsTask(database, settings, callback);
        task.execute();
    }

    public List<String> getTrackListIdentifiers() {
        return database.getTrackListIdentifiers();
    }

}
