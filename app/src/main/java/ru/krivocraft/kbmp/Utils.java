package ru.krivocraft.kbmp;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;

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

    static List<Track> search(ContentResolver contentResolver, boolean recognize) {
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        String[] projection = {
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION
        };
        final String sortOrder = MediaStore.Audio.AudioColumns.DATA + " COLLATE LOCALIZED ASC";
        List<Track> storage = new ArrayList<>();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = contentResolver.query(uri, projection, selection, null, sortOrder);
        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {

                String path = cursor.getString(0);
                String artist = cursor.getString(1);
                String title = cursor.getString(2);
                long duration = cursor.getLong(3);

                if (recognize) {
                    String[] meta = title.split(" - ");
                    if (meta.length > 1) {
                        artist = meta[0];
                        title = meta[1];
                    } else {
                        meta = title.split(" — ");
                        if (meta.length > 1) {
                            artist = meta[0];
                            title = meta[1];
                        }
                    }
                }

                cursor.moveToNext();
                if (path != null && path.endsWith(".mp3")) {
                    storage.add(new Track(duration, artist, title, path));
                }
            }
            cursor.close();
        }
        return storage;
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
