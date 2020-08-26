package ru.krivocraft.tortoise.core.base;

import ru.krivocraft.tortoise.core.api.MediaPlayer;
import ru.krivocraft.tortoise.core.api.Playback;

import java.io.IOException;

public class BasePlayback implements Playback {

    private final Metadata metadata;
    private final MediaPlayer player;

    private boolean playing;

    public BasePlayback(Metadata metadata, MediaPlayer player) {
        this.metadata = metadata;
        this.player = player;
    }

    @Override
    public void start() {
        try {
            player.set(metadata.path());
            player.start();
            playing = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void pause() {
        player.pause();
        playing = false;
    }

    @Override
    public void play() {
        player.play();
        playing = true;
    }

    @Override
    public void stop() {
        player.pause();
        player.reset();
        player.release();
    }

    @Override
    public void seekTo(long position) {
        player.seekTo((int) position);
    }

    @Override
    public State state() {
        return new BasePlaybackState(playing, 0);
    }

    @Override
    public Metadata metadata() {
        return metadata;
    }

}
