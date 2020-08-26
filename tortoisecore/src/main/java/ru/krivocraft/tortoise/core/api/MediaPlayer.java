package ru.krivocraft.tortoise.core.api;

import java.io.IOException;

public interface MediaPlayer {

    void set(String uri) throws IOException;

    void play();

    void pause();

    void seekTo(int position);

    void reset();

    void start();

    void prepared();

    void release();

}
