package ru.krivocraft.tortoise.core.api;

public interface AudioFocus {

    void request();

    void release();

    interface ChangeListener {

        void mute();

        void gain();

        void silently();

    }
}
