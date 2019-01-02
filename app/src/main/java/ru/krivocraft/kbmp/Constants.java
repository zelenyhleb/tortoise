package ru.krivocraft.kbmp;

class Constants {

    static final String ACTION_PLAY = "action_play";
    static final String ACTION_PAUSE = "action_pause";
    static final String ACTION_NEXT = "action_next";
    static final String ACTION_PREVIOUS = "action_previous";
    static final String ACTION_CLOSE = "action_close";
    static final String ACTION_SHOW_PLAYER= "action_show_player";
    static final String COMPOSITIONS = "compositions_list";

    static final String COMPOSITION_NAME = "composition_name";
    static final String COMPOSITION_AUTHOR = "composition_author";
    static final String COMPOSITION_PATH = "composition_path";
    static final String COMPOSITION_IDENTIFIER = "composition_identifier";
    static final String COMPOSITION_DURATION = "composition_duration";
    static final String COMPOSITION_PROGRESS = "composition_progress";
    static final String COMPOSITION_STATE = "composition_is_playing";

    static final String PLAYLIST_PREFIX = "playlist_";

    static final String PLAYLIST_INDEX = "playlist_index";
    static final String PLAYLISTS = "playlists";
    static final String PLAYLIST_COMPOSITION_REFERENCE = "composition_reference";

    static final String UNKNOWN_ARTIST = "Unknown Artist";
    static final String UNKNOWN_COMPOSITION = "Unknown Track";

    static final int ONE_SECOND = 1000;
    static final int ZERO = 0;

    static final int NOTIFY_ID = 124;
    static final int HEADSET_STATE_PLUG_OUT = 0;
    static final int HEADSET_STATE_PLUG_IN = 1;

    final static int INDEX_FRAGMENT_SETTINGS = 0;
    final static int INDEX_FRAGMENT_PLAYLISTGRID = 1;
    final static int INDEX_FRAGMENT_TRACKLIST = 2;

    final static int INDEX_FRAGMENT_PLAYER = 0;
    final static int INDEX_FRAGMENT_PLAYLIST = 1;
}
