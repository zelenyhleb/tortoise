package ru.krivocraft.tortoise.core.rating;

import ru.krivocraft.tortoise.core.data.TracksProvider;
import ru.krivocraft.tortoise.core.model.Track;
import ru.krivocraft.tortoise.core.model.TrackReference;


public class RatingImpl implements Rating {

    private final TracksProvider tracksStorageManager;

    public RatingImpl(TracksProvider tracksStorageManager) {
        this.tracksStorageManager = tracksStorageManager;
    }

    @Override
    public void rate(TrackReference reference, int delta) {
        Track track = tracksStorageManager.getTrack(reference);
        track.setRating(track.getRating() + delta);
        tracksStorageManager.updateTrack(track);
    }

}
