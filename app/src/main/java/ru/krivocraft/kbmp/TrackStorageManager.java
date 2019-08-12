package ru.krivocraft.kbmp;

import android.content.ContentResolver;
import android.content.SharedPreferences;

import java.util.ArrayList;

import ru.krivocraft.kbmp.constants.Constants;

class TrackStorageManager {

    private ContentResolver contentResolver;
    private OnStorageUpdateCallback callback;
    private SharedPreferences storage;
    private TrackList metaStorage;
    private boolean recognize;

    TrackStorageManager(ContentResolver contentResolver, SharedPreferences sharedPreferences, OnStorageUpdateCallback callback, boolean recognize) {
        this.callback = callback;
        this.storage = sharedPreferences;
        this.contentResolver = contentResolver;
        this.recognize = recognize;

        metaStorage = new TrackList(Constants.STORAGE_DISPLAY_NAME, new ArrayList<>(), true);
    }

    void search() {
        new GetFromDiskTask(contentResolver, recognize, metaStorage, this::notifyListener).execute();
    }

    private void notifyListener() {
        SharedPreferences.Editor editor = storage.edit();
        editor.putString(metaStorage.getIdentifier(), metaStorage.toJson());
        editor.apply();
        callback.onStorageUpdate();
    }

    TrackList getStorage() {
        return metaStorage;
    }

}


