package ru.krivocraft.kbmp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

import ru.krivocraft.kbmp.constants.Constants;

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

    static List<Track> search(CharSequence string, List<String> trackListToSearch, ContentResolver contentResolver, boolean recognize) {
        ArrayList<Track> trackList = new ArrayList<>();
        for (String path : trackListToSearch) {
            Track track = loadData(path, contentResolver, recognize);

            String formattedName = track.getTitle().toLowerCase();
            String formattedArtist = track.getArtist().toLowerCase();
            String formattedSearchStr = string.toString().toLowerCase();

            if (formattedName.contains(formattedSearchStr) || formattedArtist.contains(formattedSearchStr)) {
                trackList.add(track);
            }
        }
        return trackList;
    }

    static ArrayList<String> search(ContentResolver contentResolver) {
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        String[] projection = {
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION
        };
        final String sortOrder = MediaStore.Audio.AudioColumns.DATA + " COLLATE LOCALIZED ASC";
        ArrayList<String> storage = new ArrayList<>();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = contentResolver.query(uri, projection, selection, null, sortOrder);
        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                String path = cursor.getString(0);
                cursor.moveToNext();
                if (path != null && path.endsWith(".mp3")) {
                    storage.add(path);
                }
            }
            cursor.close();
        }
        return storage;
    }

    static Track loadData(String path, ContentResolver contentResolver, boolean recognize) {
        String selection = MediaStore.Audio.Media.DATA + " = ?";
        String[] projection = {
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION
        };
        String[] args = {
                path
        };
        String artist = Constants.UNKNOWN_ARTIST;
        String title = Constants.UNKNOWN_COMPOSITION;
        String duration = "0";

        Cursor cursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, args, MediaStore.Audio.Media.TITLE);
        if (cursor != null) {
            cursor.moveToFirst();
            artist = cursor.getString(0);
            title = cursor.getString(1);
            duration = cursor.getString(2);
            cursor.close();
        }

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

        return new Track(duration, artist, title, path);
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

    static boolean getOption(SharedPreferences preferences, String key) {
        return preferences.getBoolean(key, false);
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

    static void restart(Context context) {
        Intent mStartActivity = new Intent(context, MainActivity.class);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        if (mgr != null) {
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 500, mPendingIntent);
        }
        System.exit(0);
    }
}
