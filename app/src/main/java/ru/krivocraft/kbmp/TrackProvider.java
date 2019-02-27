package ru.krivocraft.kbmp;

import android.content.Context;
import android.os.AsyncTask;

import java.util.ArrayList;

class TrackProvider {

    private Context context;
    private OnNewTrackListListener onNewTrackListListener;

    TrackProvider(Context context, OnNewTrackListListener onNewTrackListListener) {
        this.context = context;
        this.onNewTrackListListener = onNewTrackListListener;
    }

    void search() {
        new GetFromDiskTask().execute();
    }

    private class GetFromDiskTask extends AsyncTask<Void, Integer, ArrayList<Track>> {

        @Override
        protected ArrayList<Track> doInBackground(Void... voids) {
            return Utils.search(context);
        }

        @Override
        protected void onPostExecute(ArrayList<Track> tracks) {
            super.onPostExecute(tracks);
            onNewTrackListListener.onNewTrackList(new TrackList(tracks, context, ""));
        }
    }

    interface OnNewTrackListListener {
        void onNewTrackList(TrackList trackList);
    }
}
