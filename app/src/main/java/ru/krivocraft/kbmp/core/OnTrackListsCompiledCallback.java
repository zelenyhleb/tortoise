package ru.krivocraft.kbmp.core;

import java.util.List;

import ru.krivocraft.kbmp.core.track.TrackList;

public interface OnTrackListsCompiledCallback {
    void onTrackListsCompiled(List<TrackList> trackLists);
}
