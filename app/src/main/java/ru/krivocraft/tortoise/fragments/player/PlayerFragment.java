package ru.krivocraft.tortoise.fragments.player;

import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;

public interface PlayerFragment {

    void setInitialData(MediaMetadataCompat metadata, PlaybackStateCompat state);

    void updateMediaMetadata(MediaMetadataCompat metadata);

    void updatePlaybackState(PlaybackStateCompat state);

    void showPlaybackStateChanges();

    void showMediaMetadataChanges();
}
