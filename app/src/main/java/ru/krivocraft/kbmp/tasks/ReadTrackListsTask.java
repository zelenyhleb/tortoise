package ru.krivocraft.kbmp.tasks;

import android.content.SharedPreferences;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

import ru.krivocraft.kbmp.TrackList;
import ru.krivocraft.kbmp.constants.Constants;
import ru.krivocraft.kbmp.sqlite.DBConnection;

public class ReadTrackListsTask extends AsyncTask<Void, Integer, List<TrackList>> {

    private SharedPreferences settingsStorage;
    private OnTrackListsReadCallback callback;
    private DBConnection connection;

    public ReadTrackListsTask(DBConnection connection, SharedPreferences settingsStorage, OnTrackListsReadCallback callback) {
        this.settingsStorage = settingsStorage;
        this.callback = callback;
        this.connection = connection;
    }

    @Override
    protected List<TrackList> doInBackground(Void... voids) {
        List<TrackList> allTrackLists = new ArrayList<>();
        List<TrackList> storedTrackLists = connection.getTrackLists();
        for (TrackList trackList : storedTrackLists) {
            if (trackList.getType() == Constants.TRACK_LIST_BY_AUTHOR) {
                if (settingsStorage.getBoolean(Constants.KEY_SORT_BY_ARTIST, false)) {
                    allTrackLists.add(trackList);
                }
            } else if (trackList.getType() == Constants.TRACK_LIST_BY_TAG) {
                if (settingsStorage.getBoolean(Constants.KEY_SORT_BY_TAG, false)) {
                    allTrackLists.add(trackList);
                }
            } else {
                allTrackLists.add(trackList);
            }
        }
        return allTrackLists;
    }

    @Override
    protected void onPostExecute(List<TrackList> trackLists) {
        super.onPostExecute(trackLists);
        callback.onTrackListsRead(trackLists);
    }
}
