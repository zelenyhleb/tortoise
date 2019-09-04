package ru.krivocraft.kbmp.api;

import java.util.List;

import ru.krivocraft.kbmp.TrackList;

public interface OnTrackListsCompiledCallback {
    void onTrackListsCompiled(List<TrackList> trackLists);
}
