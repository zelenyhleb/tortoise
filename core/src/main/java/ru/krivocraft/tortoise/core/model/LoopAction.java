package ru.krivocraft.tortoise.core.model;

import ru.krivocraft.tortoise.core.api.Playback;
import ru.krivocraft.tortoise.core.api.utils.Action;

public abstract class LoopAction implements Action {

    private final Playback playback;

    protected LoopAction(Playback playback) {
        this.playback = playback;
    }

    protected final Playback playback() {
        return playback;
    }

    public static final class Next extends LoopAction {

        public Next(Playback playback) {
            super(playback);
        }

        @Override
        public void execute() {
            playback().next();
        }
    }

    public static final class First extends LoopAction {

        public First(Playback playback) {
            super(playback);
        }

        @Override
        public void execute() {
            playback().skipTo(0);
        }
    }

    public static final class Stop extends LoopAction {

        public Stop(Playback playback) {
            super(playback);
        }

        @Override
        public void execute() {
            playback().stop();
        }
    }

    public static final class This extends LoopAction {

        public This(Playback playback) {
            super(playback);
        }

        @Override
        public void execute() {
            playback().skipTo(playback().tracks().indexOf(playback().current()));
        }
    }
}
