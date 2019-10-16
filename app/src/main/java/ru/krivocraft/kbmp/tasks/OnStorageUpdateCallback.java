package ru.krivocraft.kbmp.tasks;

import java.util.List;

import ru.krivocraft.kbmp.core.track.Track;

public interface OnStorageUpdateCallback {
    void onStorageUpdate(List<Track> tracks);
}
