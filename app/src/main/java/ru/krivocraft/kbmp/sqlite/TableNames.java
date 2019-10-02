package ru.krivocraft.kbmp.sqlite;

import ru.krivocraft.kbmp.TrackList;
import ru.krivocraft.kbmp.constants.Constants;

class TableNames {
    static final String TRACKS = "tracks";
    static final String TRACK_LISTS = "track_lists";
    static final String TAGS = "tags";
    static final String TAGS_TRACKS = "tags_tracks";
    static final String ALL_TRACKS = TrackList.createIdentifier(Constants.STORAGE_TRACKS_DISPLAY_NAME);
}
