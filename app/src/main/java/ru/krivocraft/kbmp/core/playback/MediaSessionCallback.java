package ru.krivocraft.kbmp.core.playback;

import android.support.v4.media.session.MediaSessionCompat;

public class MediaSessionCallback extends MediaSessionCompat.Callback {

    private final PlaybackManager playbackManager;
    private final OnStopCallback onStopCallback;

    public MediaSessionCallback(PlaybackManager playbackManager, OnStopCallback onStopCallback) {
        this.playbackManager = playbackManager;
        this.onStopCallback = onStopCallback;
    }

    @Override
    public void onPlay() {
        playbackManager.play();
    }

    @Override
    public void onPause() {
        playbackManager.pause();
    }

    @Override
    public void onSkipToNext() {
        playbackManager.nextTrack();
    }

    @Override
    public void onSkipToPrevious() {
        playbackManager.previousTrack();
    }

    @Override
    public void onSeekTo(long pos) {
        playbackManager.seekTo((int) pos);
    }

    @Override
    public void onSkipToQueueItem(long id) {
        playbackManager.newTrack((int) id);
    }

    @Override
    public void onStop() {
        onStopCallback.onStop();
    }

    interface OnStopCallback {
        void onStop();
    }
}
