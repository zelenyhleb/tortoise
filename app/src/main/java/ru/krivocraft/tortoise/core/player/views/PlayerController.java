package ru.krivocraft.tortoise.core.player.views;

public interface PlayerController {
    void onPlay();

    void onPause();

    void onNext();

    void onPrevious();

    void onSeekTo(int position);

}
