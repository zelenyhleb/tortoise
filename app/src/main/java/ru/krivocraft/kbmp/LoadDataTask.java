package ru.krivocraft.kbmp;

import android.content.ContentResolver;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

public class LoadDataTask extends AsyncTask<String, Integer, List<Track>> {

    private DataLoaderCallback dataLoaderCallback;
    private ProgressCallback progressCallback;
    private ContentResolver contentResolver;

    void setDataLoaderCallback(DataLoaderCallback dataLoaderCallback) {
        this.dataLoaderCallback = dataLoaderCallback;
    }

    void setProgressCallback(ProgressCallback progressCallback) {
        this.progressCallback = progressCallback;
    }

    void setContentResolver(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    @Override
    protected List<Track> doInBackground(String... strings) {
        List<Track> tracks = new ArrayList<>();
        for (String path : strings) {
            Track track = Utils.loadData(path, contentResolver);
            tracks.add(track);
            publishProgress(tracks.indexOf(track));
        }
        return tracks;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        if (progressCallback != null) {
            progressCallback.onProgressUpdated(values[0]);
        }
    }

    @Override
    protected void onPostExecute(List<Track> tracks) {
        super.onPostExecute(tracks);
        if (dataLoaderCallback != null) {
            dataLoaderCallback.onDataLoaded(tracks);
        }
    }

    interface DataLoaderCallback {
        void onDataLoaded(List<Track> tracks);
    }

    interface ProgressCallback {
        void onProgressUpdated(int progress);
    }
}
