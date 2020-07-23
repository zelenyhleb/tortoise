package ru.krivocraft.tortoise.core.data;

import ru.krivocraft.tortoise.core.model.Track;
import ru.krivocraft.tortoise.core.model.TrackReference;

import java.util.List;

public interface TracksProvider {

    List<Track> getTracks(List<TrackReference> references);

    List<TrackReference> getReferences(List<Track> tracks);

    Track getTrack(TrackReference reference);

    void updateTrack(Track track);

    List<Track> getTrackStorage();
}
