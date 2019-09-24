package ru.krivocraft.kbmp;

import android.content.Context;
import android.content.SharedPreferences;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ru.krivocraft.kbmp.constants.Constants;

import static android.content.Context.MODE_PRIVATE;

public class Tracks {

    /*
    *   Decodes tracks and returns ArrayList of decoded tracks
    * */
    public static List<Track> getTrackStorage(Context context) {
        List<String> encodedTracks = collectEncodedTracks(context);
        return new ArrayList<>(CollectionUtils.collect(encodedTracks, Track::fromJson));
    }

    /*
     *  Collects encoded track data from storage and returns LinkedList of json-encoded tracks
     * */
    private static List<String> collectEncodedTracks(Context context) {
        List<String> encodedTracks = new LinkedList<>();
        Map<String, ?> storage = context.getSharedPreferences(Constants.STORAGE_TRACKS, MODE_PRIVATE).getAll();
        for (Map.Entry<String, ?> map : storage.entrySet()) {
            encodedTracks.add(map.getValue().toString());
        }
        return encodedTracks;
    }

    static void updateTrackStorage(Context context, List<Track> tracks) {
        for (int i = 0; i < tracks.size(); i++) {
            updateTrack(context, new TrackReference(tracks.get(i)), tracks.get(i));
        }
    }

    public static List<Track> getTracks(Context context, List<TrackReference> references) {
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
        List<Track> trackStorage = getTrackStorage(context);
        List<String> paths = new ArrayList<>(CollectionUtils.collect(trackStorage, Track::getPath));
        return new TrackReference(trackStorage.get(paths.indexOf(path)));
    }

    static TrackReference getReference(Context context, Track track) {
        return getReference(context, track.getPath());
    }

    public static List<TrackReference> getReferences(Context context, List<Track> tracks) {
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
