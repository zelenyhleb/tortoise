package ru.krivocraft.kbmp.sqlite;

import ru.krivocraft.kbmp.core.storage.TrackListsStorageManager;
import ru.krivocraft.kbmp.core.track.TrackList;

class TableNames {
    static final String TRACKS = "tracks";
    static final String TRACK_LISTS = "track_lists";
    static final String TAGS = "tags";
    static final String TAGS_TRACKS = "tags_tracks";
    static final String ALL_TRACKS = TrackList.createIdentifier(TrackListsStorageManager.STORAGE_TRACKS_DISPLAY_NAME);
}
