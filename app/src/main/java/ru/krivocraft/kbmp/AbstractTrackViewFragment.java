package ru.krivocraft.kbmp;

import android.support.v4.app.Fragment;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;

abstract class AbstractTrackViewFragment extends Fragment implements Track.StateCallback {

    public AbstractTrackViewFragment() {

    }

    abstract void invalidate();

    @Override
    public void onMetadataChanged(MediaMetadataCompat metadata) {
        invalidate();
    }

    @Override
    public void onPlaybackStateChanged(PlaybackStateCompat playbackState) {
        invalidate();
    }
}
