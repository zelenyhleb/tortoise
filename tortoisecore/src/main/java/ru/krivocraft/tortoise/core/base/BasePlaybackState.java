package ru.krivocraft.tortoise.core.base;

import ru.krivocraft.tortoise.core.api.Playback;

public class BasePlaybackState implements Playback.State {

    private final boolean playing;
    private final int position;

    public BasePlaybackState(boolean playing, int position) {
        this.playing = playing;
        this.position = position;
    }

    @Override
    public boolean playing() {
        return playing;
    }

    @Override
    public int position() {
        return position;
    }
}
