package ru.krivocraft.tortoise.core.rating;

import ru.krivocraft.tortoise.core.model.Track;
import ru.krivocraft.tortoise.core.model.TrackReference;
import ru.krivocraft.tortoise.core.tracklist.TracksStorageManager;

public class RatingImpl implements Rating {

    private final TracksStorageManager tracksStorageManager;

    public RatingImpl(TracksStorageManager tracksStorageManager) {
        this.tracksStorageManager = tracksStorageManager;
    }

    @Override
    public void rate(TrackReference reference, int delta) {
        Track track = tracksStorageManager.getTrack(reference);
        track.setRating(track.getRating() + delta);
        tracksStorageManager.updateTrack(track);
    }

}
