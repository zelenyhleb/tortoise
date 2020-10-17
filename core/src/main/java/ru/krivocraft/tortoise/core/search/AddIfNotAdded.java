package ru.krivocraft.tortoise.core.search;

import ru.krivocraft.tortoise.core.model.Track;

import java.util.List;
import java.util.function.Consumer;

final class AddIfNotAdded implements Consumer<Track.Reference> {
    private final List<Track.Reference> references;

    AddIfNotAdded(List<Track.Reference> references) {
        this.references = references;
    }

    @Override
    public void accept(Track.Reference reference) {
        if (!references.contains(reference)) {
            references.add(reference);
        }
    }
}
