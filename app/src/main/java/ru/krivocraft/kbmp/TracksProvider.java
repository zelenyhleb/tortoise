package ru.krivocraft.kbmp;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

import ru.krivocraft.kbmp.api.TrackListsStorageManager;
import ru.krivocraft.kbmp.constants.Constants;
import ru.krivocraft.kbmp.tasks.GetFromDiskTask;
import ru.krivocraft.kbmp.tasks.OnTrackListsReadCallback;

import static android.content.Context.MODE_PRIVATE;

class TracksProvider {

    private final Context context;
    private ContentResolver contentResolver;
    private SharedPreferences storage;
    private List<Track> tracksFromStorage;
    private TrackListsStorageManager trackListsStorageManager;
    private boolean recognize;

    TracksProvider(Context context) {
        this.storage = context.getSharedPreferences(Constants.STORAGE_TRACKS, MODE_PRIVATE);
        this.contentResolver = context.getContentResolver();
        this.trackListsStorageManager = new TrackListsStorageManager(context);

        SettingsManager settingsManager = new SettingsManager(context);
        this.recognize = settingsManager.getOption(Constants.KEY_RECOGNIZE_NAMES, true);

        this.context = context;
        tracksFromStorage = new ArrayList<>();
    }

    void search() {
        new GetFromDiskTask(contentResolver, recognize, tracksFromStorage, this::manageStorage).execute();
    }

    private void manageStorage() {
        List<TrackReference> allTracks = new ArrayList<>();

        SharedPreferences.Editor tracksEditor = storage.edit();

        removeNonExistingTracks(Tracks.getTrackStorage(context), tracksEditor);
        addNewTracks(allTracks, Tracks.getTrackStorage(context), tracksEditor);
        tracksEditor.apply();

        writeRootTrackList(allTracks);

        notifyTracksStorageChanged();
    }

    private void notifyTracksStorageChanged() {
        context.sendBroadcast(new Intent(Constants.Actions.ACTION_UPDATE_STORAGE));
    }

    private void writeRootTrackList(List<TrackReference> allTracks) {
        TrackList trackList = new TrackList(Constants.STORAGE_TRACKS_DISPLAY_NAME, allTracks, Constants.TRACK_LIST_CUSTOM);
        trackListsStorageManager.writeTrackList(trackList);
    }

    private void addNewTracks(List<TrackReference> allTracks, List<Track> existingTracks, SharedPreferences.Editor tracksEditor) {
        for (int i = 0; i < tracksFromStorage.size(); i++) {
            TrackReference reference = new TrackReference(i);
            Track track = tracksFromStorage.get(i);

            if (!existingTracks.contains(track)) {
                tracksEditor.putString(reference.toString(), track.toJson());
            }

            allTracks.add(reference);
        }
    }

    private void removeNonExistingTracks(List<Track> existingTracks, SharedPreferences.Editor tracksEditor) {
        List<TrackReference> removedReferences = new ArrayList<>();
        for (int i = 0; i < existingTracks.size(); i++) {
            TrackReference reference = new TrackReference(i);
            Track track = existingTracks.get(i);

            if (!tracksFromStorage.contains(track)) {
                tracksEditor.remove(reference.toString());
                removedReferences.add(reference);
            }
        }
        updateTrackLists(removedReferences);
    }

    private void updateTrackLists(List<TrackReference> removedTracks) {
        trackListsStorageManager.readTrackLists(trackLists -> {
            for (TrackList trackList : trackLists) {
                removeNonExistingTracks(removedTracks, trackList);
                removeTrackListIfEmpty(trackList);
            }
        });
    }

    private void removeTrackListIfEmpty(TrackList trackList) {
        if (trackList.size() == 0) {
            trackListsStorageManager.removeTrackList(trackList);
        }
    }

    private void removeNonExistingTracks(List<TrackReference> removedTracks, TrackList trackList) {
        List<TrackReference> referencesToRemove = new ArrayList<>();
        for (TrackReference reference : trackList.getTrackReferences()) {
            if (removedTracks.contains(reference)) {
                referencesToRemove.add(reference);
            }
        }
        trackList.removeAll(referencesToRemove);
    }

}


