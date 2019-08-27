package ru.krivocraft.kbmp;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;

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

    static int getSeconds(int v) {
        return (int) Math.ceil(v / 1000.0);
    }

    static List<TrackReference> search(Context context, CharSequence string, List<TrackReference> input) {
        List<TrackReference> trackList = new ArrayList<>();
        List<Track> searched = Tracks.getTracks(context, input);
        for (Track track : searched) {

            String formattedName = track.getTitle().toLowerCase();
            String formattedArtist = track.getArtist().toLowerCase();
            String formattedSearchStr = string.toString().toLowerCase();

            if (formattedName.contains(formattedSearchStr) || formattedArtist.contains(formattedSearchStr)) {
                trackList.add(input.get(searched.indexOf(track)));
            }
        }
        return trackList;
    }

    static Bitmap loadArt(String path) {
        Bitmap art = null;

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(path);

        byte[] embeddedPicture = retriever.getEmbeddedPicture();

        if (embeddedPicture != null) {
            art = BitmapFactory.decodeByteArray(embeddedPicture, 0, embeddedPicture.length);
        }

        return art;
    }

    static boolean getOption(SharedPreferences preferences, String key, boolean defValue) {
        return preferences.getBoolean(key, defValue);
    }

    static void putOption(SharedPreferences preferences, String key, boolean value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    static void clearCache(SharedPreferences preferences) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }

}
