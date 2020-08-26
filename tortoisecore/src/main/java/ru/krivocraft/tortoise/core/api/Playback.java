package ru.krivocraft.tortoise.core.api;

public interface Playback {

    void start();

    void pause();

    void play();

    void stop();

    void seekTo(long position);

    State state();

    Metadata metadata();

    interface State {

        boolean playing();

        int position();
    }

    interface Metadata {

        String artist();

        String title();

        String path();

        int duration();

    }

    interface Callback {

        void onStateChanged(State newState);

        void onTrackChanged(Metadata newMetadata);

    }
}
