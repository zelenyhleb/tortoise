package ru.krivocraft.kbmp;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.collections4.CollectionUtils;

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

    static List<Track> getTrackStorage(Context context) {
        List<Track> tracks = new ArrayList<>();
        int i = 0;
        TrackReference reference;
        String json;
        while (true) {
            reference = new TrackReference(i);
            json = context.getSharedPreferences(Constants.TRACKS_NAME, MODE_PRIVATE).getString(reference.toString(), null);
            if (json != null) {
                tracks.add(Track.fromJson(json));
                i++;
            } else {
                break;
            }
        }
        return tracks;
    }

    static List<Track> getTracks(Context context, List<TrackReference> references) {
        List<Track> tracks = new ArrayList<>();
        for (TrackReference reference : references) {
            tracks.add(getTrack(context, reference));
        }
        return tracks;
    }

    static void updateTrack(Context context, TrackReference reference, Track track) {
        SharedPreferences.Editor editor = context.getSharedPreferences(Constants.TRACKS_NAME, MODE_PRIVATE).edit();
        editor.putString(reference.toString(), track.toJson());
        editor.apply();
    }

    static TrackReference getReference(Context context, String path) {
        return new TrackReference(new ArrayList<>(CollectionUtils.collect(getTrackStorage(context), Track::getPath)).indexOf(path));
    }

    private static TrackReference getReference(Context context, Track track) {
        return getReference(context, track.getPath());
    }

    static List<TrackReference> getReferences(Context context, List<Track> tracks){
        List<TrackReference> references = new ArrayList<>();
        for (Track track : tracks) {
            references.add(getReference(context, track));
        }
        return references;
    }

    static Track getTrack(Context context, TrackReference reference) {
        SharedPreferences preferences = context.getSharedPreferences(Constants.TRACKS_NAME, MODE_PRIVATE);
        String json = preferences.getString(reference.toString(), new Track(0, "", "", "").toJson());
        return Track.fromJson(json);
    }

    void search() {
        new GetFromDiskTask(contentResolver, recognize, metaStorage, this::notifyListener).execute();
    }

    private void notifyListener() {
        List<TrackReference> allTracks = new ArrayList<>();

        List<Track> existingTracks = getTrackStorage(context);

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


