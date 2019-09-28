package ru.krivocraft.kbmp;

import android.content.Context;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

import ru.krivocraft.kbmp.api.TracksStorageManager;

public class Searcher {

    private TracksStorageManager tracksStorageManager;

    public Searcher(Context context) {
        this.tracksStorageManager = new TracksStorageManager(context);
    }

    public List<TrackReference> search(CharSequence string, List<TrackReference> input) {
        List<TrackReference> trackList = new ArrayList<>();
        List<Track> searched = tracksStorageManager.getTracks(input);
        for (Track track : searched) {

            String formattedName = track.getTitle().toLowerCase();
            String formattedArtist = track.getArtist().toLowerCase();
            String formattedSearchStr = string.toString().toLowerCase();
            String[] tags = CollectionUtils.collect(track.getTags(), Tag::getText).toArray(new String[0]);

            if (formattedName.contains(formattedSearchStr) || formattedArtist.contains(formattedSearchStr) || checkInTags(tags, formattedSearchStr)) {
                trackList.add(input.get(searched.indexOf(track)));
            }
        }
        return trackList;
    }

    private boolean checkInTags(String[] tags, String searchString) {
        for (String tag : tags) {
            if (tag.toLowerCase().contains(searchString)) {
                return true;
            }
        }
        return false;
    }
}
