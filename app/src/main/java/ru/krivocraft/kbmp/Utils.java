package ru.krivocraft.kbmp;

import android.media.MediaMetadataRetriever;

import java.io.File;

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

    static Composition getComposition(File file, int i) {


        MediaMetadataRetriever retriever = new MediaMetadataRetriever();

        String path = file.getPath();

        retriever.setDataSource(path);

        String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        String composer = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        String name = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);


        return new Composition(duration, composer, name, path, i);
    }

    private static int id = 0;

    static void searchRecursively(File directory, Composition.OnCompositionFoundListener listener) {
        File[] files = directory.listFiles();
        for (File file : files) {
            String fileName = file.getName();
            System.out.println("searching in " + fileName);
            if (file.isDirectory()) {
                searchRecursively(file, listener);
            } else {
                if (fileName.endsWith(".mp3") && !fileName.startsWith("2_")) {
                    listener.onCompositionFound(getComposition(file, id));
                    id++;
                }
            }
        }
        System.out.println("search completed in " + directory);
    }
}
