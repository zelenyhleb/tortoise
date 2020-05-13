package ru.krivocraft.tortoise.core.rating;

import ru.krivocraft.tortoise.core.model.Track;
import ru.krivocraft.tortoise.core.model.TrackList;
import ru.krivocraft.tortoise.core.model.TrackReference;
import ru.krivocraft.tortoise.core.tracklist.TracksStorageManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Shuffle {

    private final TracksStorageManager tracksStorageManager;

    public Shuffle(TracksStorageManager tracksStorageManager) {
        this.tracksStorageManager = tracksStorageManager;
    }

    public List<TrackReference> shuffle(TrackList trackList, TrackReference firstTrack) {
        trackList.getTrackReferences().remove(firstTrack);
        List<TrackReference> references = shuffle(trackList);
        references.add(0, firstTrack);
        return references;
    }



    public List<TrackReference> shuffle(List<TrackReference> trackReferences) {
        List<Track> tracks = tracksStorageManager.getTracks(trackReferences);
        Random random = new Random(System.currentTimeMillis());
        List<Track> shuffled = new ArrayList<>();

        List<Integer> pool = new ArrayList<>();
        for (Track track : tracks) {
            for (int i = 0; i <= track.getRating(); i++) {
                pool.add(tracks.indexOf(track));
            }
        }

        while (pool.size() > 0) {
            int index = pool.get(random.nextInt(pool.size()));
            shuffled.add(tracks.get(index));
            pool.removeAll(Collections.singleton(index));
        }
        return tracksStorageManager.getReferences(shuffled);
    }

    public List<TrackReference> shuffle(TrackList trackList) {
        return shuffle(trackList.getTrackReferences());
    }

}
