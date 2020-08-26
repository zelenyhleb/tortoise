package ru.krivocraft.tortoise.core.player;

import android.support.v4.media.session.PlaybackStateCompat;
import ru.krivocraft.tortoise.core.model.Track;

import java.util.function.Consumer;

public final class PlayerStateCallback {

    private final Consumer<PlaybackStateCompat> stateChanged;
    private final Consumer<Track> trackChanged;

    public PlayerStateCallback(Consumer<PlaybackStateCompat> stateChanged, Consumer<Track> trackChanged) {
        this.stateChanged = stateChanged;
        this.trackChanged = trackChanged;
    }

    public void onPlaybackStateChanged(PlaybackStateCompat stateCompat) {
        stateChanged.accept(stateCompat);
    }

    public void onTrackChanged(Track track) {
        trackChanged.accept(track);
    }
}
