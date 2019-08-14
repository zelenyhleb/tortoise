package ru.krivocraft.kbmp;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

import ru.krivocraft.kbmp.constants.Constants;

import static android.content.Context.MODE_PRIVATE;

class TrackStorageManager {

    private final Context context;
    private ContentResolver contentResolver;
    private SharedPreferences storage;
    private SharedPreferences trackLists;
    private List<Track> metaStorage;
    private boolean recognize;

    TrackStorageManager(Context context) {
        this.storage = context.getSharedPreferences(Constants.TRACKS_NAME, MODE_PRIVATE);
        this.trackLists = context.getSharedPreferences(Constants.TRACK_LISTS_NAME, MODE_PRIVATE);
        this.contentResolver = context.getContentResolver();
        this.recognize = Utils.getOption(context.getSharedPreferences(Constants.SETTINGS_NAME, MODE_PRIVATE), Constants.KEY_RECOGNIZE_NAMES, true);

        this.context = context;
        metaStorage = new ArrayList<>();
    }

    void search() {
        new GetFromDiskTask(contentResolver, recognize, metaStorage, this::manageStorage).execute();
    }

    private void manageStorage() {
        List<TrackReference> allTracks = new ArrayList<>();

        List<Track> existingTracks = Tracks.getTrackStorage(context);

        SharedPreferences.Editor tracksEditor = storage.edit();
        for (int i = 0; i < metaStorage.size(); i++) {
            TrackReference reference = new TrackReference(i);
            Track track = metaStorage.get(i);

            if (!existingTracks.contains(track)) {
                tracksEditor.putString(reference.toString(), track.toJson());
            }

            allTracks.add(reference);
        }
        tracksEditor.apply();

        TrackList trackList = new TrackList(Constants.STORAGE_DISPLAY_NAME, allTracks, true);
        SharedPreferences.Editor trackListsEditor = trackLists.edit();
        trackListsEditor.putString(trackList.getIdentifier(), trackList.toJson());
        trackListsEditor.apply();

        context.sendBroadcast(new Intent(Constants.Actions.ACTION_UPDATE_STORAGE));

    }

}


