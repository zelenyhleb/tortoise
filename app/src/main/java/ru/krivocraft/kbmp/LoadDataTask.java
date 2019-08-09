package ru.krivocraft.kbmp;

import android.content.ContentResolver;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

public class LoadDataTask extends AsyncTask<String, Void, List<Track>> {

    private DataLoaderCallback callback;
    private ContentResolver contentResolver;

    void setCallback(DataLoaderCallback callback) {
        this.callback = callback;
    }

    void setContentResolver(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    @Override
    protected List<Track> doInBackground(String... strings) {
        List<Track> tracks = new ArrayList<>();
        for (String path : strings) {
            tracks.add(Utils.loadData(path, contentResolver));
        }
        return tracks;
    }

    @Override
    protected void onPostExecute(List<Track> tracks) {
        super.onPostExecute(tracks);
        callback.onDataLoaded(tracks);
    }

    interface DataLoaderCallback {
        void onDataLoaded(List<Track> track);
    }
}
