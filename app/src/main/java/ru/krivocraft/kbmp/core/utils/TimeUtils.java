package ru.krivocraft.kbmp.core.utils;

public class TimeUtils {
    public static String getFormattedTime(int time) {

        int seconds = time % 60;
        int minutes = (time - seconds) / 60;

        String formattedSeconds = String.valueOf(seconds);
        String formattedMinutes = String.valueOf(minutes);

        if (seconds < 10) {
            formattedSeconds = "0" + formattedSeconds;
        }

        if (minutes < 10) {
            formattedMinutes = "0" + formattedMinutes;
        }


        return formattedMinutes + ":" + formattedSeconds;
    }

    public static int getSeconds(int v) {
        return (int) Math.ceil(v / 1000.0);
    }

}
