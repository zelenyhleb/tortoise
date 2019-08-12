package ru.krivocraft.kbmp;

import android.content.ContentResolver;
import android.os.AsyncTask;

import java.util.List;

class GetFromDiskTask extends AsyncTask<Void, Integer, List<Track>> {

    private ContentResolver contentResolver;
    private TrackList metaStorage;
    private OnStorageUpdateCallback callback;
    private boolean recognize;

    GetFromDiskTask(ContentResolver contentResolver, boolean recognize, TrackList storage, OnStorageUpdateCallback callback) {
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
        for (Track track : tracks) {
            metaStorage.addTrack(track);
        }
        callback.onStorageUpdate();
    }
}
