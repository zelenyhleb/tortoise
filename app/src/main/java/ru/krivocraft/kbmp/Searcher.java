package ru.krivocraft.kbmp;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import ru.krivocraft.kbmp.api.TrackListsStorageManager;
import ru.krivocraft.kbmp.api.TracksStorageManager;

public class Searcher {

    private final TracksStorageManager tracksStorageManager;
    private final TrackListsStorageManager trackListsStorageManager;

    public Searcher(Context context) {
        this.tracksStorageManager = new TracksStorageManager(context);
        this.trackListsStorageManager = new TrackListsStorageManager(context);
    }

    public List<TrackReference> search(CharSequence string, List<TrackReference> input) {
        List<TrackReference> references = new ArrayList<>();
        List<Track> searched = tracksStorageManager.getTracks(input);
        List<TrackList> trackLists = trackListsStorageManager.readTrackLists(false, false);
        for (Track track : searched) {

            String formattedName = track.getTitle().toLowerCase();
            String formattedArtist = track.getArtist().toLowerCase();
            String formattedSearchStr = string.toString().toLowerCase();

            if (formattedName.contains(formattedSearchStr) || formattedArtist.contains(formattedSearchStr)) {
                references.add(input.get(searched.indexOf(track)));
            }
        }

        for (TrackList trackList : trackLists) {
            if (trackList.getDisplayName().contains(string)) {
                for (TrackReference reference : trackList.getTrackReferences()) {
                    if (!references.contains(reference)) {
                        references.add(reference);
                    }
                }
            }
        }
        return references;
    }

    public List<Track> searchInTracks(CharSequence string, List<Track> input) {
        List<Track> found = new ArrayList<>();
        List<TrackList> trackLists = trackListsStorageManager.readTrackLists(false, false);
        for (Track track : input) {

            String formattedName = track.getTitle().toLowerCase();
            String formattedArtist = track.getArtist().toLowerCase();
            String formattedSearchStr = string.toString().toLowerCase();
            if (formattedName.contains(formattedSearchStr) || formattedArtist.contains(formattedSearchStr)) {
                found.add(input.get(input.indexOf(track)));
            }
        }

        for (TrackList trackList : trackLists) {
            if (trackList.getDisplayName().contains(string)) {
                for (TrackReference trackReference : trackList.getTrackReferences()) {
                    Track track = tracksStorageManager.getTrack(trackReference);
                    if (!found.contains(track)) {
                        found.add(track);
                    }
                }
            }
        }
        return found;
    }
}
