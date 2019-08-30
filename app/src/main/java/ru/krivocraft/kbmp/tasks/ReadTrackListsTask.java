package ru.krivocraft.kbmp.tasks;

import android.content.SharedPreferences;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ru.krivocraft.kbmp.TrackList;
import ru.krivocraft.kbmp.constants.Constants;

public class ReadTrackListsTask extends AsyncTask<Void, Integer, List<TrackList>> {

    private SharedPreferences trackListsStorage;
    private SharedPreferences settingsStorage;
    private OnTrackListsReadCallback callback;

    public ReadTrackListsTask(SharedPreferences trackListsStorage, SharedPreferences settingsStorage, OnTrackListsReadCallback callback) {
        this.trackListsStorage = trackListsStorage;
        this.settingsStorage = settingsStorage;
        this.callback = callback;
    }

    @Override
    protected List<TrackList> doInBackground(Void... voids) {
        List<TrackList> allTrackLists = new ArrayList<>();
        Map<String, ?> trackLists = trackListsStorage.getAll();
        for (Map.Entry<String, ?> entry : trackLists.entrySet()) {
            TrackList trackList = TrackList.fromJson((String) entry.getValue());
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
