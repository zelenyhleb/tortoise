package ru.krivocraft.tortoise.core.rating;

import ru.krivocraft.tortoise.core.model.Track;
import ru.krivocraft.tortoise.core.model.TrackList;
import ru.krivocraft.tortoise.core.model.TrackReference;
import ru.krivocraft.tortoise.core.settings.SettingsStorageManager;
import ru.krivocraft.tortoise.core.tracklist.TracksStorageManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Shuffle {

    private final TracksStorageManager tracksStorageManager;
    private final SettingsStorageManager settings;

    public Shuffle(TracksStorageManager tracksStorageManager, SettingsStorageManager settings) {
        this.tracksStorageManager = tracksStorageManager;
        this.settings = settings;
    }

    public List<TrackReference> shuffle(TrackList trackList, TrackReference firstTrack) {
        trackList.getTrackReferences().remove(firstTrack);
        List<TrackReference> references =
                settings.getOption(SettingsStorageManager.KEY_SMART_SHUFFLE, true) ?
                        smartShuffle(trackList) : basicShuffle(trackList);
        references.add(0, firstTrack);
        return references;
    }

    private List<TrackReference> basicShuffle(TrackList trackList) {
        Collections.shuffle(trackList.getTrackReferences());
        return trackList.getTrackReferences();
    }

    private List<TrackReference> smartShuffle(List<TrackReference> trackReferences) {
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

    private List<TrackReference> smartShuffle(TrackList trackList) {
        return smartShuffle(trackList.getTrackReferences());
    }

}
