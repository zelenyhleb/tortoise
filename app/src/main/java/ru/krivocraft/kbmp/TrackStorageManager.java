package ru.krivocraft.kbmp;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import ru.krivocraft.kbmp.constants.Constants;

import static android.content.Context.MODE_PRIVATE;

class TrackStorageManager {

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

        metaStorage = new ArrayList<>();
    }

    static List<Track> getTrackStorage(Context context) {
        return fromJson(context.getSharedPreferences(Constants.TRACKS_NAME, MODE_PRIVATE)
                .getString(Constants.TRACKS_NAME, toJson(new ArrayList<>())));
    }

    static Track getTrack(Context context, TrackReference reference) {
        SharedPreferences preferences = context.getSharedPreferences(Constants.TRACKS_NAME, MODE_PRIVATE);
        return Track.fromJson(preferences.getString(reference.toString(), new Track(0, "", "", "").toJson()));
    }

    void search() {
        new GetFromDiskTask(contentResolver, recognize, metaStorage, this::notifyListener).execute();
    }

    private void notifyListener() {
        List<TrackReference> allTracks = new ArrayList<>();

        SharedPreferences.Editor tracksEditor = storage.edit();
        for (int i = 0; i < metaStorage.size(); i++) {
            TrackReference reference = new TrackReference(i);
            allTracks.add(reference);
            tracksEditor.putString(reference.toString(), metaStorage.get(i).toJson());
        }
        tracksEditor.apply();

        TrackList trackList = new TrackList(Constants.STORAGE_DISPLAY_NAME, allTracks, true);
        SharedPreferences.Editor trackListsEditor = trackLists.edit();
        trackListsEditor.putString(trackList.getIdentifier(), trackList.toJson());
        trackListsEditor.apply();

    }

    private static String toJson(List<Track> metaStorage) {
        return new Gson().toJson(metaStorage);
    }

    static List<Track> fromJson(String json) {
        return new Gson().fromJson(json, new TypeToken<List<Track>>() {
        }.getType());
    }

}


