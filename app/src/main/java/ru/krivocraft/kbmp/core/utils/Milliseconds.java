package ru.krivocraft.kbmp.core.utils;

public class Milliseconds {
    private final int millis;

    public Milliseconds(int millis) {
        //todo: consider throwing IllegalArgumentException for negative values
        this.millis = millis;
    }

    public int seconds() {
        return (int) Math.ceil(millis / 1000f);
    }
}
