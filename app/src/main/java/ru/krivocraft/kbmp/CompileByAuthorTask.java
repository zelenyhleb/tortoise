package ru.krivocraft.kbmp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompileByAuthorTask extends CompileTrackListsTask {

    @Override
    protected Map<String, List<Track>> doInBackground(Track... source) {
        Map<String, List<Track>> playlistMap = new HashMap<>();
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
        return playlistMap;
    }

}
