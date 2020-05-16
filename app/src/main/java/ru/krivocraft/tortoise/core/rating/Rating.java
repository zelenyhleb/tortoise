package ru.krivocraft.tortoise.core.rating;

import ru.krivocraft.tortoise.core.model.TrackReference;

public interface Rating {

    void rate(TrackReference track, int delta);
}
