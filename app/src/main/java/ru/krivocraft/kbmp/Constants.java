package ru.krivocraft.kbmp;

class Constants {

    static final String ACTION_SHOW_PLAYER= "action_show_player";
    static final String ACTION_UPDATE_TRACK_LIST = "action_update_track_list";
    static final String ACTION_REQUEST_TRACK_LIST= "action_request_track_list";
    static final String ACTION_UPDATE_STORAGE = "action_update_storage";
    static final String ACTION_REQUEST_DATA = "request_position";
    static final String ACTION_RESULT_DATA = "result_position";
    static final String ACTION_SHUFFLE = "shuffle";
    static final String ACTION_PLAY_FROM_LIST= "play_from_list";

    static final String EXTRA_POSITION = "position";
    static final String EXTRA_METADATA = "metadata";
    static final String EXTRA_PATH = "path";
    static final String EXTRA_PLAYBACK_STATE = "playback_state";
    static final String EXTRA_TRACK_LIST = "tracklist_extra";

    static final String PLAYLIST_PREFIX = "playlist_";

    static final String PLAYLIST_INDEX = "playlist_index";
    static final String PLAYLISTS = "playlists";
    static final String PLAYLIST_COMPOSITION_REFERENCE = "composition_reference";

    static final String UNKNOWN_ARTIST = "Unknown Artist";
    static final String UNKNOWN_COMPOSITION = "Unknown Track";

    static final int ONE_SECOND = 1000;

    static final int NOTIFY_ID = 124;
    static final int HEADSET_STATE_PLUG_OUT = 0;
    static final int HEADSET_STATE_PLUG_IN = 1;

    final static int INDEX_FRAGMENT_SETTINGS = 0;
    final static int INDEX_FRAGMENT_PLAYLISTGRID = 1;
    final static int INDEX_FRAGMENT_TRACKLIST = 2;

    final static int INDEX_FRAGMENT_PLAYER = 0;
    final static int INDEX_FRAGMENT_PLAYLIST = 1;

}
