package ru.krivocraft.kbmp;

import android.support.v4.media.session.MediaSessionCompat;

public class MediaSessionCallback extends MediaSessionCompat.Callback {
    private final PlaybackManager playbackManager;

    public MediaSessionCallback(PlaybackManager playbackManager) {
        this.playbackManager = playbackManager;
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
}
