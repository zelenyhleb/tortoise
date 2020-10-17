package ru.krivocraft.tortoise.core.search;

import ru.krivocraft.tortoise.core.model.Track;

import java.util.Optional;
import java.util.function.Function;

final class TrackEntry implements Function<Track, Entry> {

    @Override
    public Entry apply(Track track) {
        String artist = Optional.ofNullable(track.getArtist()).orElse("").toLowerCase();
        String title = Optional.ofNullable(track.getTitle()).orElse("").toLowerCase();
        return new Entry(track, artist + " " + title);
    }
}
