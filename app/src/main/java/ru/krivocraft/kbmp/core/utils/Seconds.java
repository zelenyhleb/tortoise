package ru.krivocraft.kbmp.core.utils;

public class Seconds {
    private final int seconds;

    public Seconds(int seconds) {
        //todo: consider throwing IllegalArgumentException for negative values
        this.seconds = seconds;
    }

    public String formatted() {
        int secondsTail = seconds % 60;
        int minutes = (seconds - secondsTail) / 60;
        return twoDigits(minutes) + ":" + twoDigits(secondsTail);
    }

    private String twoDigits(int numerical) {
        String formatted = String.valueOf(numerical);
        if (numerical < 10) {
            formatted = "0" + formatted;
        }
        return formatted;
    }
}
