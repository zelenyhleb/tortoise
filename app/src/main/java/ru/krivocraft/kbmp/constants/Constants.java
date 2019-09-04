package ru.krivocraft.kbmp.constants;

public class Constants {

    public static final String UNKNOWN_ARTIST = "Unknown Artist";
    public static final String UNKNOWN_COMPOSITION = "Unknown Track";

    public static final int ONE_SECOND = 1000;

    public static final String STORAGE_TRACKS_DISPLAY_NAME = "All tracks";
    public static final String FAVORITES_DISPLAY_NAME = "Favorites";

    public static final String STORAGE_TRACK_LISTS = "trackLists";
    public static final String STORAGE_TRACKS = "tracks";
    public static final String STORAGE_SETTINGS = "settings";
    public static final String STORAGE_TAGS = "tags";

    public static final String KEY_TAGS = "allTags";

    public static final int TRACK_LIST_CUSTOM = 91;
    public static final int TRACK_LIST_BY_AUTHOR= 92;
    public static final int TRACK_LIST_BY_TAG = 93;


    public static final String KEY_THEME = "useAlternativeTheme";
    public static final String KEY_SORT_BY_ARTIST = "sortByArtist";
    public static final String KEY_SORT_BY_TAG = "sortByTag";
    public static final String KEY_CLEAR_CACHE = "clearCache";
    public static final String KEY_RECOGNIZE_NAMES = "recognizeNames";
    public static final String KEY_OLD_TRACK_LISTS_EXIST = "oldExist";

    public static final String SHUFFLE_STATE = "shuffle_state";
    public static final int STATE_SHUFFLED = 120;
    public static final int STATE_UNSHUFFLED = 121;

    public static final String LOOP_TYPE = "loop_type";
    public static final int LOOP_TRACK = 122;
    public static final int LOOP_TRACK_LIST = 123;
    public static final int NOT_LOOP = 124;

    public static class Actions {
        public static final String ACTION_SHOW_PLAYER = "action_show_player";
        public static final String ACTION_HIDE_PLAYER = "action_hide_player";
        public static final String ACTION_UPDATE_TRACK_LIST = "action_update_track_list";
        public static final String ACTION_REQUEST_TRACK_LIST = "action_request_track_list";
        public static final String ACTION_UPDATE_STORAGE = "action_update_storage";
        public static final String ACTION_REQUEST_DATA = "request_position";
        public static final String ACTION_RESULT_DATA = "result_position";
        public static final String ACTION_SHUFFLE = "shuffle";
        public static final String ACTION_EDIT_PLAYING_TRACK_LIST = "edit_current_track_list";
        public static final String ACTION_REQUEST_STOP = "stop";
        public static final String ACTION_PLAY_FROM_LIST = "play_from_list";
        public static final String ACTION_RESULT_TRACK_LIST = "result_track_list";
        public static final String ACTION_EDIT_TRACK_LIST = "edit_track_list";
    }

    public static class Extras {
        public static final String EXTRA_POSITION = "position";
        public static final String EXTRA_METADATA = "metadata";
        public static final String EXTRA_TRACK = "path";
        public static final String EXTRA_CURSOR = "cursor";
        public static final String EXTRA_PLAYBACK_STATE = "playback_state";
        public static final String EXTRA_TRACK_LIST = "track_list_extra";
    }
}
