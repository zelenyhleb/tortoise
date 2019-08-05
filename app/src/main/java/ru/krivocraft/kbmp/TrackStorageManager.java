package ru.krivocraft.kbmp;

import android.content.ContentResolver;
import android.os.AsyncTask;

import java.util.ArrayList;

class TrackStorageManager extends StorageManager {
    private ContentResolver contentResolver;
    private ArrayList<String> metaStorage;

    TrackStorageManager(ContentResolver contentResolver, OnStorageUpdateCallback callback) {
        super(callback);
        this.contentResolver = contentResolver;
        this.metaStorage = new ArrayList<>();
    }

    void search() {
        new GetFromDiskTask(contentResolver, metaStorage, new StorageManager.OnStorageUpdateCallback() {
            @Override
            public void onStorageUpdate() {
                notifyListener();
            }
        }).execute();
    }

    private static class GetFromDiskTask extends AsyncTask<Void, Integer, ArrayList<String>> {
        private ContentResolver contentResolver;
        private ArrayList<String> metaStorage;
        private OnStorageUpdateCallback callback;

        GetFromDiskTask(ContentResolver contentResolver, ArrayList<String> storage, OnStorageUpdateCallback callback) {
            this.contentResolver = contentResolver;
            this.metaStorage = storage;
            this.callback = callback;
        }

        @Override
        protected ArrayList<String> doInBackground(Void... voids) {
            return Utils.search(contentResolver);
        }

        @Override
        protected void onPostExecute(ArrayList<String> tracks) {
            super.onPostExecute(tracks);
            metaStorage.addAll(tracks);
            callback.onStorageUpdate();
        }
    }

    ArrayList<String> getStorage() {
        return metaStorage;
    }

}
