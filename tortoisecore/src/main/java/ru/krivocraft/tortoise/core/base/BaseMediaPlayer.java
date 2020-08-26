package ru.krivocraft.tortoise.core.base;

import ru.krivocraft.tortoise.core.api.MediaPlayer;

import java.io.IOException;

public class BaseMediaPlayer implements MediaPlayer {

    private final android.media.MediaPlayer player;

    public BaseMediaPlayer(android.media.MediaPlayer player) {
        this.player = player;
    }

    @Override
    public void set(String uri) throws IOException {
        player.setDataSource(uri);
    }

    @Override
    public void play() {
        player.start();
    }

    @Override
    public void pause() {
        player.pause();
    }

    @Override
    public void seekTo(int position) {
        player.seekTo(position);
    }

    @Override
    public void reset() {
        player.reset();
    }

    @Override
    public void start() {
        player.setOnPreparedListener(player -> prepared());
        player.prepareAsync();
    }

    @Override
    public void prepared() {

    }

    @Override
    public void release() {
        player.release();
    }

}
