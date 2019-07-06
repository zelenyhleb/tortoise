package ru.krivocraft.kbmp;

import android.content.ContentResolver;
import android.content.Context;
import android.os.AsyncTask;

import java.util.ArrayList;

class TrackProvider {
    private Context context;
    private OnUpdateCallback callback;
    private ArrayList<String> metaStorage;

    TrackProvider(Context context, OnUpdateCallback callback) {
        this.context = context;
        this.callback = callback;
        this.metaStorage = new ArrayList<>();
    }

    void search() {
        new GetFromDiskTask(context.getContentResolver(), metaStorage, new OnUpdateCallback() {
            @Override
            public void onUpdate() {
                callback.onUpdate();
            }
        }).execute();
    }

    private static class GetFromDiskTask extends AsyncTask<Void, Integer, ArrayList<String>> {
        private ContentResolver contentResolver;
        private ArrayList<String> metaStorage;
        private OnUpdateCallback callback;

        GetFromDiskTask(ContentResolver contentResolver, ArrayList<String> storage, OnUpdateCallback callback) {
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
            callback.onUpdate();
        }
    }

    ArrayList<String> getStorage() {
        return metaStorage;
    }

    interface OnUpdateCallback {
        void onUpdate();
    }
}
