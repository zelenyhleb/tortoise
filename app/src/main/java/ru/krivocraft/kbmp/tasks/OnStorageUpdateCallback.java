package ru.krivocraft.kbmp.tasks;

import java.util.List;

import ru.krivocraft.kbmp.Track;

public interface OnStorageUpdateCallback {
    void onStorageUpdate(List<Track> tracks);
}
