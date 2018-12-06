package ru.krivocraft.kbmp;

import android.content.Context;

class SearchTaskBundle {

    Context context;
    Track.OnTracksFoundListener listener;
    Playlist allTracksPlaylist;

    SearchTaskBundle(Context context, Track.OnTracksFoundListener listener, Playlist allTracksPlaylist) {
        this.context = context;
        this.listener = listener;
        this.allTracksPlaylist = allTracksPlaylist;
    }
}
