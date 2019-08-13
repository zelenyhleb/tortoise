package ru.krivocraft.kbmp;

import android.content.ContentResolver;
import android.os.AsyncTask;

import java.util.List;

class GetFromDiskTask extends AsyncTask<Void, Integer, List<Track>> {

    private ContentResolver contentResolver;
    private List<Track> metaStorage;
    private OnStorageUpdateCallback callback;
    private boolean recognize;

    GetFromDiskTask(ContentResolver contentResolver, boolean recognize, List<Track> storage, OnStorageUpdateCallback callback) {
        this.contentResolver = contentResolver;
        this.recognize = recognize;
        this.metaStorage = storage;
        this.callback = callback;
    }

    @Override
    protected List<Track> doInBackground(Void... voids) {
        return Utils.search(contentResolver, recognize);
    }

    @Override
    protected void onPostExecute(List<Track> tracks) {
        super.onPostExecute(tracks);
        metaStorage.addAll(tracks);
        callback.onStorageUpdate();
    }
}
