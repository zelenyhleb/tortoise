package ru.krivocraft.kbmp;

import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompileTrackListsTask extends AsyncTask<TrackList, Integer, List<TrackList>> {

    private OnTrackListsCompiledListener listener;
    private ContentResolver contentResolver;
    private SharedPreferences preferences;
    private boolean recognizeNames;

    @Override
    protected List<TrackList> doInBackground(TrackList... trackLists) {
        Map<String, TrackList> playlistMap = new HashMap<>();
        TrackList source = trackLists[0];
        if (source != null) {
            for (String track : source.getTracks()) {
                String artist = Utils.loadData(track, contentResolver, recognizeNames).getArtist();
                TrackList trackList = playlistMap.get(artist);
                if (trackList == null) {
                    trackList = new TrackList(artist, new ArrayList<>(), false);
                    playlistMap.put(artist, trackList);
                }
                if (!trackList.getTracks().contains(track)) {
                    trackList.addTrack(track);
                }
            }
        }
        writeTrackLists(playlistMap.values());
        return new ArrayList<>(playlistMap.values());
    }

    @Override
    protected void onPostExecute(List<TrackList> trackLists) {
        super.onPostExecute(trackLists);
        listener.onTrackListsCompiled(trackLists);
    }

    void setListener(OnTrackListsCompiledListener listener) {
        this.listener = listener;
    }

    void setContentResolver(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    void setRecognizeNames(boolean recognizeNames) {
        this.recognizeNames = recognizeNames;
    }

    void setPreferences(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    private void writeTrackLists(Collection<TrackList> trackLists) {
        SharedPreferences.Editor editor = preferences.edit();
        for (TrackList trackList : trackLists) {
            editor.putString(trackList.getIdentifier(), trackList.toJson());
        }
        editor.apply();
    }

    interface OnTrackListsCompiledListener {
        void onTrackListsCompiled(List<TrackList> trackLists);
    }
}
