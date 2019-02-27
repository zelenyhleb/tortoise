package ru.krivocraft.kbmp;

import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;

interface StateCallback {
    void onMetadataChanged(MediaMetadataCompat metadata);

    void onPlaybackStateChanged(PlaybackStateCompat playbackState);
}
