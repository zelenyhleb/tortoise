package ru.krivocraft.kbmp;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaMetadataRetriever;

import java.io.File;
import java.util.*;

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

    static List<Composition> searchRecursively(File directory) {
        List<Composition> compositions = new ArrayList<>();
        File[] files = directory.listFiles();
        for (File file : files) {
            System.out.println("searching in " + file.getPath());
            if (file.isDirectory()) {
                compositions.addAll(searchRecursively(file));
            } else {
                if (file.getPath().endsWith(".mp3"))
                    compositions.add(getComposition(file, id));
                id++;
            }
        }
        System.out.println("search completed");
        return compositions;
    }

    static void putPath(Context context, String path) {
        SharedPreferences sp = context.getSharedPreferences(Constants.COMPOSITION_LIST, Context.MODE_PRIVATE);

        Set<String> compositionsSet = getPaths(context);

        if (!new ArrayList<>(compositionsSet).contains(path)) {
            compositionsSet.add(path);
        }

        SharedPreferences.Editor editor = sp.edit();
        editor.putStringSet(Constants.COMPOSITION_LIST, compositionsSet);
        editor.apply();
    }

    static Set<String> getPaths(Context context) {
        SharedPreferences sp = context.getSharedPreferences(Constants.COMPOSITION_LIST, Context.MODE_PRIVATE);
        return sp.getStringSet(Constants.COMPOSITION_LIST, new HashSet<String>());
    }
}
