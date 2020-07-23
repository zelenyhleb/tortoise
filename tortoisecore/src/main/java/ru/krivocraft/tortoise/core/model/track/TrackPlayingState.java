package ru.krivocraft.tortoise.core.model.track;

public class TrackPlayingState {
    private final boolean selected;
    private final boolean playing;

    public TrackPlayingState(boolean selected, boolean playing) {
        this.selected = selected;
        this.playing = playing;
    }

    public boolean isSelected() {
        return selected;
    }

    public boolean isPlaying() {
        return playing;
    }
}
