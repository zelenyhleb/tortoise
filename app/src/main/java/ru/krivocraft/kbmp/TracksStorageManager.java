package ru.krivocraft.kbmp;

import android.content.ContentResolver;

public class TracksStorageManager extends StorageManager {

    TracksStorageManager(ContentResolver contentResolver, OnStorageUpdateCallback callback) {
        super(callback);

    }
}
