package ru.krivocraft.kbmp;

import android.content.ContentResolver;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

class TrackStorageManager {

    private ContentResolver contentResolver;
    private OnStorageUpdateCallback callback;
    private SharedPreferences storage;
    private TrackList metaStorage;

    TrackStorageManager(ContentResolver contentResolver, SharedPreferences sharedPreferences, OnStorageUpdateCallback callback) {
        this.callback = callback;
        this.storage = sharedPreferences;
        this.contentResolver = contentResolver;
        metaStorage = new TrackList(Constants.STORAGE_DISPLAY_NAME, new ArrayList<>());
    }

    void search() {
        new GetFromDiskTask(contentResolver, metaStorage, this::notifyListener).execute();
    }

    private void notifyListener() {
        SharedPreferences.Editor editor = storage.edit();
        editor.putString(metaStorage.getIdentifier(), metaStorage.toJson());
        editor.apply();
        callback.onStorageUpdate();
    }

    List<String> getStorage() {
        return metaStorage.getPaths();
    }

}


