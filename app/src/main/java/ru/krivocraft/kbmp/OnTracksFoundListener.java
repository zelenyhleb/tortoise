package ru.krivocraft.kbmp;

import java.util.List;

interface OnTracksFoundListener {
    void onTrackSearchingCompleted(List<Track> tracks);
}
