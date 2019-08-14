package ru.krivocraft.kbmp;

import android.content.Context;
import android.content.SharedPreferences;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ru.krivocraft.kbmp.constants.Constants;

import static android.content.Context.MODE_PRIVATE;

class Tracks {
    static List<Track> getTrackStorage(Context context) {
        List<Track> tracks = new ArrayList<>();
        int i = 0;
        TrackReference reference;
        String json;
        while (true) {
            reference = new TrackReference(i);
            json = context.getSharedPreferences(Constants.STORAGE_TRACKS, MODE_PRIVATE).getString(reference.toString(), null);
            if (json != null) {
                tracks.add(Track.fromJson(json));
                i++;
            } else {
                break;
            }
        }
        return tracks;
    }

    static void updateTrackStorage(Context context, List<Track> tracks) {
        for (int i = 0; i < tracks.size(); i++) {
            updateTrack(context, new TrackReference(i), tracks.get(i));
        }
    }

    static List<Track> getTracks(Context context, List<TrackReference> references) {
        List<Track> tracks = new ArrayList<>();
        for (TrackReference reference : references) {
            tracks.add(getTrack(context, reference));
        }
        return tracks;
    }

    static void updateTrack(Context context, TrackReference reference, Track track) {
        SharedPreferences.Editor editor = context.getSharedPreferences(Constants.STORAGE_TRACKS, MODE_PRIVATE).edit();
        editor.putString(reference.toString(), track.toJson());
        editor.apply();
    }

    static void updateTracks(Context context, Map<TrackReference, Track> trackMap) {
        for (Map.Entry<TrackReference, Track> entry : trackMap.entrySet()) {
            updateTrack(context, entry.getKey(), entry.getValue());
        }
    }

    static TrackReference getReference(Context context, String path) {
        return new TrackReference(new ArrayList<>(CollectionUtils.collect(getTrackStorage(context), Track::getPath)).indexOf(path));
    }

    static TrackReference getReference(Context context, Track track) {
        return getReference(context, track.getPath());
    }

    static List<TrackReference> getReferences(Context context, List<Track> tracks) {
        List<TrackReference> references = new ArrayList<>();
        for (Track track : tracks) {
            references.add(getReference(context, track));
        }
        return references;
    }

    static Track getTrack(Context context, TrackReference reference) {
        SharedPreferences preferences = context.getSharedPreferences(Constants.STORAGE_TRACKS, MODE_PRIVATE);
        String json = preferences.getString(reference.toString(), new Track(0, "", "", "").toJson());
        return Track.fromJson(json);
    }
}
