package ru.krivocraft.kbmp;

import android.content.Context;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ListView;

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
    static void search(Context context, Track.OnTracksFoundListener listener) {
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        String[] projection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION
        };
        final String sortOrder = MediaStore.Audio.AudioColumns.TITLE + " COLLATE LOCALIZED ASC";
        List<Track> tracks = new ArrayList<>();

        Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = context.getContentResolver().query(uri, projection, selection, null, sortOrder);
        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                String title = cursor.getString(0);
                String artist = cursor.getString(1);
                String path = cursor.getString(2);
                String songDuration = cursor.getString(4);
                cursor.moveToNext();
                if (path != null && path.endsWith(".mp3")) {
                    tracks.add(new Track(songDuration, artist, title, path, id));
                    id++;
                }
            }
            listener.onTrackSearchingCompleted(tracks);
            cursor.close();
        }
    }
}
