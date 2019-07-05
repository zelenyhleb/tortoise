package ru.krivocraft.kbmp;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    static Bitmap getTrackBitmap(String path) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(path);

        byte[] artBytes = retriever.getEmbeddedPicture();
        Bitmap bm = null;

        if (artBytes != null) {
            bm = BitmapFactory.decodeByteArray(artBytes, 0, artBytes.length);
        }

        return bm;
    }

    static TrackList search(CharSequence string, TrackList trackListToSearch) {
        TrackList trackList = new TrackList("temp");
        for (Track track : trackListToSearch.getTracks()) {

            String formattedName = track.getTitle().toLowerCase();
            String formattedArtist = track.getArtist().toLowerCase();
            String formattedSearchStr = string.toString().toLowerCase();

            if (formattedName.contains(formattedSearchStr) || formattedArtist.contains(formattedSearchStr)) {
                trackList.addTrack(track);
            }
        }
        return trackList;
    }

    static List<TrackList> compilePlaylistsByAuthor(TrackList allTracksTrackList) {
        Map<String, TrackList> playlistMap = new HashMap<>();
        for (Track track : allTracksTrackList.getTracks()) {
            TrackList trackList = playlistMap.get(track.getArtist());
            if (trackList == null) {
                trackList = new TrackList(track.getArtist());
                playlistMap.put(track.getArtist(), trackList);
            }
            if (!trackList.contains(track)) {
                trackList.addTrack(track);
            }
        }
        return new ArrayList<>(playlistMap.values());
    }

    private static int id = 0;

    static ArrayList<Track> search(ContentResolver contentResolver, TrackList existingTracks) {
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        String[] projection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION
        };
        final String sortOrder = MediaStore.Audio.AudioColumns.TITLE + " COLLATE LOCALIZED ASC";
        ArrayList<Track> tracks = new ArrayList<>();

        Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = contentResolver.query(uri, projection, selection, null, sortOrder);
        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                String title = cursor.getString(0);
                String artist = cursor.getString(1);
                String path = cursor.getString(2);
                String songDuration = cursor.getString(4);
                cursor.moveToNext();
                if (path != null && path.endsWith(".mp3")) {
                    Track track = new Track(songDuration, artist, title, path, id, getTrackBitmap(path));
                    if (!existingTracks.contains(track)) {
                        tracks.add(track);
                    }
                    id++;
                }
            }
            cursor.close();
        }
        return tracks;
    }

    static Track loadData(String path) {
        System.out.println("1:" + String.valueOf(System.currentTimeMillis()));
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(path);

        byte[] embeddedPicture = retriever.getEmbeddedPicture();

        String title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        String artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        Bitmap art = BitmapFactory.decodeByteArray(embeddedPicture, 0, embeddedPicture.length);

        System.out.println("2: " + String.valueOf(System.currentTimeMillis()));

        return new Track(duration, artist, title, path, 0, art);
    }
}
