package ru.krivocraft.kbmp;

import android.content.SharedPreferences;
import android.support.v4.media.session.MediaSessionCompat;

import com.google.gson.Gson;

public class SharedStorageManager {

    private static final String KEY_LAST_TRACK = "last_track";
    private static final String KEY_SESSION_TOKEN = "session_token";

    static void writeToCache(SharedPreferences storage, Track track) {
        Gson gson = new Gson();
        String jsonTrack = gson.toJson(track);
        SharedPreferences.Editor editor = storage.edit();
        editor.putString(KEY_LAST_TRACK, jsonTrack);
        editor.apply();
    }

    static Track readFromCache(SharedPreferences storage) {
        Gson gson = new Gson();
        String json = storage.getString(KEY_LAST_TRACK, null);
        return gson.fromJson(json, Track.class);
    }

    static void writeToken(SharedPreferences storage, MediaSessionCompat.Token token) {
        Gson gson = new Gson();
        String jsonTrack = gson.toJson(token);
        SharedPreferences.Editor editor = storage.edit();
        editor.putString(KEY_SESSION_TOKEN, jsonTrack);
        editor.apply();
    }

    static MediaSessionCompat.Token readToken(SharedPreferences storage){
        Gson gson = new Gson();
        String json = storage.getString(KEY_SESSION_TOKEN, null);
        return gson.fromJson(json, MediaSessionCompat.Token.class);
    }
}
