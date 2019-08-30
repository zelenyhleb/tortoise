package ru.krivocraft.kbmp.tasks;

import java.util.List;

import ru.krivocraft.kbmp.TrackList;

public interface OnTrackListsReadCallback {
    void onTrackListsRead(List<TrackList> trackLists);
}
