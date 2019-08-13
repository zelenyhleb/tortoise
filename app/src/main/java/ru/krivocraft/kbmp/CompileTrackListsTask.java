package ru.krivocraft.kbmp;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompileTrackListsTask extends AsyncTask<List<Track>, Integer, Map<String, List<Track>>> {

    private OnTrackListsCompiledListener listener;

    void setListener(OnTrackListsCompiledListener listener) {
        this.listener = listener;
    }

    @Override
    protected Map<String, List<Track>> doInBackground(List<Track>... lists) {
        Map<String, List<Track>> playlistMap = new HashMap<>();
        List<Track> source = lists[0];
        if (source != null) {
            for (Track track : source) {
                String artist = track.getArtist();
                List<Track> trackList = playlistMap.get(artist);
                if (trackList == null) {
                    trackList = new ArrayList<>();
                    playlistMap.put(artist, trackList);
                }
                if (!trackList.contains(track)) {
                    trackList.add(track);
                }
            }
        }
        return playlistMap;
    }

    @Override
    protected void onPostExecute(Map<String, List<Track>> stringListMap) {
        super.onPostExecute(stringListMap);
        listener.onTrackListsCompiled(stringListMap);
    }

    interface OnTrackListsCompiledListener {
        void onTrackListsCompiled(Map<String, List<Track>> trackLists);
    }
}
