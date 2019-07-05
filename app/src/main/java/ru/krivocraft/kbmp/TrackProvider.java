package ru.krivocraft.kbmp;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import java.util.ArrayList;

class TrackProvider {
    private Context context;
    private TrackList storage;
    private OnUpdateCallback callback;

    TrackProvider(Context context, OnUpdateCallback callback) {
        this.context = context;
        this.callback = callback;
        this.storage = new TrackList("storage");
    }

    void search() {
        new GetFromDiskTask(context.getContentResolver(), storage, new OnUpdateCallback() {
            @Override
            public void onUpdate() {
                callback.onUpdate();
            }
        }).execute();
    }

    private static class GetFromDiskTask extends AsyncTask<Void, Integer, ArrayList<Track>> {
        private ContentResolver contentResolver;
        private TrackList storage;
        private OnUpdateCallback callback;

        GetFromDiskTask(ContentResolver contentResolver, TrackList storage, OnUpdateCallback callback) {
            this.contentResolver = contentResolver;
            this.storage = storage;
            this.callback = callback;
        }

        @Override
        protected ArrayList<Track> doInBackground(Void... voids) {
            return Utils.search(contentResolver, storage);
        }

        @Override
        protected void onPostExecute(ArrayList<Track> tracks) {
            super.onPostExecute(tracks);
            storage.addTracks(tracks);
            callback.onUpdate();
        }
    }

    TrackList getStorage() {
        return storage;
    }

    Track findTrackByIndex(int index) {
        return storage.getTrack(index);
    }

    Track findTrackDyId(int id) {
        int low = 0;
        int high = storage.getSize() - 1;
        int mid;

        int foundId;

        while (low <= high) {
            mid = (low + high) / 2;
            foundId = Integer.parseInt(storage.getTrack(mid).getIdentifier());
            if (foundId == id) {
                return findTrackByIndex(foundId);
            } else if (mid > id) {
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }
        return null;
    }

    interface OnUpdateCallback {
        void onUpdate();
    }

}
