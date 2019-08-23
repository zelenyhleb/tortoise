package ru.krivocraft.kbmp;

import java.util.List;
import java.util.Map;

interface OnTrackListsCompiledListener {
    void onTrackListsCompiled(Map<String, List<Track>> trackLists);
}
