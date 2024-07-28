package ru.krivocraft.tortoise.core.api;

import java.util.List;

import ru.krivocraft.tortoise.core.model.Track;

public record Stamp(List<Track.Reference> order, int index, int position) {

}
