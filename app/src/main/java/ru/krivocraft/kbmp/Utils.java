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

    static int getSeconds(int v) {
        return (int) Math.ceil(v / 1000.0);
    }

    static Track getComposition(File file, int i) {


        MediaMetadataRetriever retriever = new MediaMetadataRetriever();

        String path = file.getPath();

        retriever.setDataSource(path);

        String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        String composer = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        String name = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);


        return new Track(duration, composer, name, path, i);
    }

    private static int id = 0;

    static void searchRecursively(File directory, Track.OnTrackFoundListener listener) {
        File[] files = directory.listFiles();
        for (File file : files) {
            String fileName = file.getName();
            if (file.isDirectory()) {
                searchRecursively(file, listener);
                System.out.println("search completed in " + directory);
            } else {
                if (fileName.endsWith(".mp3")) {
                    Track track = getComposition(file, id);
                    if (!track.getName().equals(Constants.UNKNOWN_COMPOSITION) && !track.getArtist().equals(Constants.UNKNOWN_ARTIST)) {
                        System.out.println("found " + fileName);
                        listener.onTrackFound(track);
                        id++;
                    }
                }
            }
        }
    }
}
