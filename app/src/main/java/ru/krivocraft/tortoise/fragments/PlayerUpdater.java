package ru.krivocraft.tortoise.fragments;

import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;

public interface PlayerUpdater {
    void onStateChanged(PlaybackStateCompat state);

    void onMetadataChanged(MediaMetadataCompat metadata);
}
