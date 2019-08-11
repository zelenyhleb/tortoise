package ru.krivocraft.kbmp.constants;

public class Constants {

    public static final String UNKNOWN_ARTIST = "Unknown Artist";
    public static final String UNKNOWN_COMPOSITION = "Unknown Track";

    public static final int ONE_SECOND = 1000;

    public static final String STORAGE_DISPLAY_NAME = "All tracks";

    public static final String TRACK_LISTS_NAME = "trackLists";
    public static final String SETTINGS_NAME = "settings";

    public static final String KEY_THEME = "useAlternativeTheme";

    public static final String SHUFFLE_STATE = "shuffle_state";
    public static final int STATE_SHUFFLED = 120;
    public static final int STATE_UNSHUFFLED = 121;

    public static final String LOOP_TYPE = "loop_type";
    public static final int LOOP_TRACK = 122;
    public static final int LOOP_TRACK_LIST = 123;
    public static final int NOT_LOOP = 124;

    public static class Actions {
        public static final String ACTION_SHOW_PLAYER= "action_show_player";
        public static final String ACTION_HIDE_PLAYER= "action_hide_player";
        public static final String ACTION_UPDATE_TRACK_LIST = "action_update_track_list";
        public static final String ACTION_REQUEST_TRACK_LIST= "action_request_track_list";
        public static final String ACTION_UPDATE_STORAGE = "action_update_storage";
        public static final String ACTION_REQUEST_DATA = "request_position";
        public static final String ACTION_RESULT_DATA = "result_position";
        public static final String ACTION_SHUFFLE = "shuffle";
        public static final String ACTION_UNSHUFFLE = "unshuffle";
        public static final String ACTION_REQUEST_STOP = "stop";
        public static final String ACTION_PLAY_FROM_LIST= "play_from_list";
        public static final String ACTION_RESULT_TRACK_LIST = "result_track_list";
    }

    public static class Extras {
        public static final String EXTRA_POSITION = "position";
        public static final String EXTRA_METADATA = "metadata";
        public static final String EXTRA_PATH = "path";
        public static final String EXTRA_PLAYBACK_STATE = "playback_state";
        public static final String EXTRA_TRACK_LIST = "track_list_extra";
    }
}
