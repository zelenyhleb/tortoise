package ru.krivocraft.kbmp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompileByTagsTask extends CompileTrackListsTask {
    @Override
    protected Map<String, List<Track>> doInBackground(Track... source) {
        Map<String, List<Track>> playlistMap = new HashMap<>();
        for (Track track : source) {
            List<Tag> tags = track.getTags();
            for (Tag tag : tags) {
                List<Track> trackList = playlistMap.get(tag.text);
                if (trackList == null) {
                    trackList = new ArrayList<>();
                    playlistMap.put(tag.text, trackList);
                }
                if (!trackList.contains(track)) {
                    trackList.add(track);
                }
            }
        }
        return playlistMap;
    }
}
