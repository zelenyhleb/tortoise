package ru.krivocraft.kbmp.tasks.compilers;

import java.util.List;
import java.util.Map;

import ru.krivocraft.kbmp.Track;

public interface OnTrackListsCompileTaskCompleted {
    void onTrackListsCompiled(Map<String, List<Track>> trackLists);
}
