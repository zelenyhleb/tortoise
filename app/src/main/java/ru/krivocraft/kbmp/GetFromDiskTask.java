package ru.krivocraft.kbmp;

import android.content.ContentResolver;
import android.os.AsyncTask;

import java.util.List;

class GetFromDiskTask extends AsyncTask<Void, Integer, List<String>> {

    private ContentResolver contentResolver;
    private TrackList metaStorage;
    private OnStorageUpdateCallback callback;

    GetFromDiskTask(ContentResolver contentResolver, TrackList storage, OnStorageUpdateCallback callback) {
        this.contentResolver = contentResolver;
        this.metaStorage = storage;
        this.callback = callback;
    }

    @Override
    protected List<String> doInBackground(Void... voids) {
        return Utils.search(contentResolver);
    }

    @Override
    protected void onPostExecute(List<String> paths) {
        super.onPostExecute(paths);
        for (String path : paths) {
            metaStorage.addTrack(Utils.loadData(path, contentResolver));
        }
        callback.onStorageUpdate();
    }
}
