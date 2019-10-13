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

        String formattedSeconds = String.valueOf(secondsTail);
        String formattedMinutes = String.valueOf(minutes);

        if (secondsTail < 10) {
            formattedSeconds = "0" + formattedSeconds;
        }

        if (minutes < 10) {
            formattedMinutes = "0" + formattedMinutes;
        }


        return formattedMinutes + ":" + formattedSeconds;
    }
}
