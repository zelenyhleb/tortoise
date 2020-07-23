package ru.krivocraft.tortoise.core.data;

import ru.krivocraft.tortoise.core.model.TrackList;

import java.util.List;

public interface TrackListsProvider {

    List<TrackList> readAllTrackLists();

}
