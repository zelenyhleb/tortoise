package ru.krivocraft.tortoise.core.model;

import androidx.core.util.Supplier;
import ru.krivocraft.tortoise.core.api.Playback;

import java.util.Objects;

public abstract class LoopType implements Supplier<Integer> {

    private final int type;
    private final Playback playback;

    protected LoopType(int type, Playback playback) {
        this.type = type;
        this.playback = playback;
    }

    public abstract LoopAction action();

    protected final Playback playback() {
        return playback;
    }

    @Override
    public final Integer get() {
        return type;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoopType loopType = (LoopType) o;
        return type == loopType.type;
    }

    @Override
    public final int hashCode() {
        return Objects.hash(type);
    }

    public static final class Track extends LoopType {
        public Track(Playback playback) {
            super(122, playback);
        }

        @Override
        public LoopAction action() {
            return new LoopAction.This(playback());
        }
    }

    public static final class Playlist extends LoopType {

        public Playlist(Playback playback) {
            super(123, playback);
        }

        @Override
        public LoopAction action() {
            if (playback().tracks().indexOf(playback().current()) + 1 < playback().tracks().size()) {
                return new LoopAction.Next(playback());
            } else {
                return new LoopAction.First(playback());
            }
        }
    }

    public static final class None extends LoopType {

        public None(Playback playback) {
            super(124, playback);
        }

        @Override
        public LoopAction action() {
            if (playback().tracks().indexOf(playback().current()) + 1 < playback().tracks().size()) {
                return new LoopAction.Next(playback());
            } else {
                return new LoopAction.Stop(playback());
            }
        }
    }

    public static final class Of implements Supplier<LoopType> {

        private final int type;
        private final Playback playback;

        public Of(int type, Playback playback) {
            this.type = type;
            this.playback = playback;
        }

        @Override
        public LoopType get() {
            switch (type) {
                case 122:
                    return new Track(playback);
                case 123:
                    return new Playlist(playback);
                default:
                    return new None(playback);
            }
        }
    }

}
