package ru.krivocraft.tortoise.core.search;

import ru.krivocraft.tortoise.core.model.Track;

final class Entry {
    private final Track track;
    private final String value;

    Entry(Track track, String value) {
        this.track = track;
        this.value = value;
    }

    public String value() {
        return value;
    }

    public Track track() {
        return track;
    }
}
