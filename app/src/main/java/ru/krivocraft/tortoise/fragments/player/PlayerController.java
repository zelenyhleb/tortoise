package ru.krivocraft.tortoise.fragments.player;

public interface PlayerController {
    void onPlay();

    void onPause();

    void onNext();

    void onPrevious();

    void onSeekTo(int position);

}
