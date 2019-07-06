package ru.krivocraft.kbmp;

import android.content.ContentResolver;
import android.content.Context;
import android.os.AsyncTask;

import java.util.ArrayList;

class TrackProvider {
    private Context context;
    private OnStorageUpdateCallback callback;
    private ArrayList<String> metaStorage;

    TrackProvider(Context context, OnStorageUpdateCallback callback) {
        this.context = context;
        this.callback = callback;
        this.metaStorage = new ArrayList<>();
    }

    void search() {
        new GetFromDiskTask(context.getContentResolver(), metaStorage, new OnStorageUpdateCallback() {
            @Override
            public void onStorageUpdate() {
                callback.onStorageUpdate();
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

    interface OnStorageUpdateCallback {
        void onStorageUpdate();
    }
}
