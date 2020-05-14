package ru.krivocraft.tortoise.core.rating;

import ru.krivocraft.tortoise.core.model.Track;

public interface TrackActionAnalyzer {

    void onTrackSkipped(Track track);

    void onTrackReplacedNearer(Track track);

    void onTrackReplacedBack(Track track);

    void onTrackRemoved(Track track);

    void onTrackPlayedFromList(Track track);
}
