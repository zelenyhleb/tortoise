package ru.krivocraft.kbmp;

import android.content.ContentResolver;
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

    static List<Track> search(CharSequence string, List<String> trackListToSearch, ContentResolver contentResolver) {
        ArrayList<Track> trackList = new ArrayList<>();
        for (String path : trackListToSearch) {
            Track track = loadData(path, contentResolver);

            String formattedName = track.getTitle().toLowerCase();
            String formattedArtist = track.getArtist().toLowerCase();
            String formattedSearchStr = string.toString().toLowerCase();

            if (formattedName.contains(formattedSearchStr) || formattedArtist.contains(formattedSearchStr)) {
                trackList.add(track);
            }
        }
        return trackList;
    }

//    static List<TrackList> compilePlaylistsByAuthor(TrackList allTracksTrackList) {
//        Map<String, TrackList> playlistMap = new HashMap<>();
//        for (Track track : allTracksTrackList.getTracks()) {
//            TrackList trackList = playlistMap.get(track.getArtist());
//            if (trackList == null) {
//                trackList = new TrackList(track.getArtist());
//                playlistMap.put(track.getArtist(), trackList);
//            }
//            if (!trackList.contains(track)) {
//                trackList.addTrack(track);
//            }
//        }
//        return new ArrayList<>(playlistMap.values());
//    }

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

        Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
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

    static Track loadData(String path, ContentResolver contentResolver) {

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

}
