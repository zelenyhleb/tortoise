package ru.krivocraft.kbmp;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

import ru.krivocraft.kbmp.api.TrackListsStorageManager;
import ru.krivocraft.kbmp.api.TracksStorageManager;
import ru.krivocraft.kbmp.constants.Constants;
import ru.krivocraft.kbmp.tasks.GetFromDiskTask;

class TracksProvider {

    private final Context context;
    private ContentResolver contentResolver;

    private TrackListsStorageManager trackListsStorageManager;
    private TracksStorageManager tracksStorageManager;

    private boolean recognize;

    TracksProvider(Context context) {
        this.contentResolver = context.getContentResolver();
        this.trackListsStorageManager = new TrackListsStorageManager(context);
        this.tracksStorageManager = new TracksStorageManager(context);

        SettingsManager settingsManager = new SettingsManager(context);
        this.recognize = settingsManager.getOption(Constants.KEY_RECOGNIZE_NAMES, true);

        this.context = context;
    }

    void search() {
        new GetFromDiskTask(contentResolver, recognize, this::manageStorage).execute();
    }

    private void manageStorage(List<Track> tracks) {
        List<TrackReference> allTracks = new ArrayList<>();

        removeNonExistingTracksFromStorage(tracksStorageManager.getTrackStorage(), tracks);
        addNewTracks(allTracks, tracksStorageManager.getTrackStorage(), tracks);

        writeRootTrackList(allTracks);

        notifyTracksStorageChanged();
    }

    private void notifyTracksStorageChanged() {
        context.sendBroadcast(new Intent(Constants.Actions.ACTION_UPDATE_STORAGE));
    }

    private void writeRootTrackList(List<TrackReference> allTracks) {
        TrackList trackList = new TrackList(Constants.STORAGE_TRACKS_DISPLAY_NAME, allTracks, Constants.TRACK_LIST_CUSTOM);
        trackListsStorageManager.updateRootTrackList(trackList);
    }

    private void addNewTracks(List<TrackReference> allTracks, List<Track> existingTracks, List<Track> readTracks) {
        for (int i = 0; i < readTracks.size(); i++) {
            Track track = readTracks.get(i);
            TrackReference reference = new TrackReference(track);

            if (!existingTracks.contains(track)) {
                tracksStorageManager.writeTrack(track);
                allTracks.add(reference);
            }
        }
    }

    private void removeNonExistingTracksFromStorage(List<Track> existingTracks, List<Track> readTracks) {
        List<TrackReference> removedReferences = new ArrayList<>();
        for (int i = 0; i < existingTracks.size(); i++) {
            Track track = existingTracks.get(i);
            TrackReference reference = new TrackReference(track);

            if (!readTracks.contains(track)) {
                tracksStorageManager.removeTrack(track);
                removedReferences.add(reference);
            }
        }
        updateTrackLists(removedReferences);
    }

    private void updateTrackLists(List<TrackReference> removedTracks) {
        List<TrackList> trackLists = trackListsStorageManager.readAllTrackLists();
        for (TrackList trackList : trackLists) {
            removeNonExistingTracksFromTrackList(removedTracks, trackList);
            removeTrackListIfEmpty(trackList);
        }
    }

    private void removeTrackListIfEmpty(TrackList trackList) {
        if (trackList.size() == 0 && !trackList.getDisplayName().equals(Constants.STORAGE_TRACKS_DISPLAY_NAME)) {
            trackListsStorageManager.removeTrackList(trackList);
        }
    }

    private void removeNonExistingTracksFromTrackList(List<TrackReference> removedTracks, TrackList trackList) {
        List<TrackReference> referencesToRemove = new ArrayList<>();
        for (TrackReference reference : trackList.getTrackReferences()) {
            if (removedTracks.contains(reference)) {
                referencesToRemove.add(reference);
            }
        }
        trackList.removeAll(referencesToRemove);
        trackListsStorageManager.removeTracks(trackList, referencesToRemove);
    }

}


