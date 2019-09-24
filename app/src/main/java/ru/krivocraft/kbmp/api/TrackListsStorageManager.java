package ru.krivocraft.kbmp.api;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ru.krivocraft.kbmp.TrackList;
import ru.krivocraft.kbmp.constants.Constants;
import ru.krivocraft.kbmp.tasks.OnTrackListsReadCallback;
import ru.krivocraft.kbmp.tasks.ReadTrackListsTask;

public class TrackListsStorageManager {

    private Context context;

    public TrackListsStorageManager(@NonNull Context context) {
        this.context = context;
    }

    public void writeTrackLists(List<TrackList> trackLists) {
        for (TrackList trackList : trackLists) {
            writeTrackList(trackList);
        }
    }

    public void writeTrackList(TrackList trackList) {
        SharedPreferences.Editor editor = getTrackListStorageEditor();
        editor.putString(trackList.getIdentifier(), trackList.toJson());
        editor.apply();
    }

    public void removeTrackList(TrackList trackList) {
        SharedPreferences.Editor editor = getTrackListStorageEditor();
        editor.remove(trackList.getIdentifier());
        editor.apply();
    }

    public void readTrackLists(OnTrackListsReadCallback callback) {
        SharedPreferences trackListsStorage = getTrackListsStorage();
        SharedPreferences settingsStorage = getSettingsStorage();

        ReadTrackListsTask task = new ReadTrackListsTask(trackListsStorage, settingsStorage, callback);
        task.execute();
    }

    public List<String> getTrackListIdentifiers() {
        List<String> identifiers = new ArrayList<>();
        if (context != null) {
            SharedPreferences sharedPreferences = getTrackListsStorage();
            Map<String, ?> trackLists = sharedPreferences.getAll();
            for (Map.Entry<String, ?> entry : trackLists.entrySet()) {
                identifiers.add(entry.getKey());
            }
        }
        return identifiers;
    }

    private SharedPreferences getSettingsStorage() {
        return getPreferences(Constants.STORAGE_SETTINGS);
    }

    private SharedPreferences getTrackListsStorage() {
        return getPreferences(Constants.STORAGE_TRACK_LISTS);
    }

    private SharedPreferences getPreferences(String storageName) {
        return context.getSharedPreferences(storageName, Context.MODE_MULTI_PROCESS);
    }

    private SharedPreferences.Editor getTrackListStorageEditor() {
        return getTrackListsStorage().edit();
    }
}
