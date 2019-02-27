package ru.krivocraft.kbmp;

import android.content.Context;

class SearchTaskBundle {

    Context context;
    OnTracksFoundListener listener;
    TrackList allTracksTrackList;

    SearchTaskBundle(Context context, OnTracksFoundListener listener, TrackList allTracksTrackList) {
        this.context = context;
        this.listener = listener;
        this.allTracksTrackList = allTracksTrackList;
    }
}
