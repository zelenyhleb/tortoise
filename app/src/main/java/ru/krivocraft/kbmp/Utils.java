package ru.krivocraft.kbmp;

import android.media.MediaMetadataRetriever;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

class Utils {
    static String getFormattedTime(int time) {

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

    public static List<Composition> search(File directory) {
        File[] allFilesInDirectory = directory.listFiles();

        List<Composition> compositions = new ArrayList<>();

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();

        for (File file : allFilesInDirectory) {
            String path = file.getPath();

            if (path.endsWith(".mp3")) {
                retriever.setDataSource(path);

                String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                String composer = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                String name = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);

                compositions.add(new Composition(duration, composer, name, path));
            }

        }

        return compositions;
    }
}
