package ru.krivocraft.kbmp;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
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

    static Bitmap getTrackBitmap(File file) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(file.getPath());

        byte[] artBytes = retriever.getEmbeddedPicture();
        Bitmap bm = null;

        if (artBytes != null) {
            bm = BitmapFactory.decodeByteArray(artBytes, 0, artBytes.length);
        }

        return bm;
    }

    static TrackList search(CharSequence string, TrackList trackListToSearch) {
        TrackList trackList = new TrackList(trackListToSearch.getContext(), "temp");
        for (Track track : trackListToSearch.getTracks()) {

            String formattedName = track.getName().toLowerCase();
            String formattedArtist = track.getArtist().toLowerCase();
            String formattedSearchStr = string.toString().toLowerCase();

            if (formattedName.contains(formattedSearchStr) || formattedArtist.contains(formattedSearchStr)) {
                trackList.addTrack(track);
            }
        }
        return trackList;
    }

    static List<TrackList> compilePlaylistsByAuthor(TrackList allTracksTrackList) {
        Context context = allTracksTrackList.getContext();
        Map<String, TrackList> playlistMap = new HashMap<>();
        for (Track track : allTracksTrackList.getTracks()) {
            TrackList trackList = playlistMap.get(track.getArtist());
            if (trackList == null) {
                trackList = new TrackList(context, track.getArtist());
                playlistMap.put(track.getArtist(), trackList);
            }
            if (!trackList.contains(track)) {
                trackList.addTrack(track);
            }
        }
        return new ArrayList<>(playlistMap.values());
    }

    private static int id = 0;

    static List<Track> search(Context context, TrackList existingTracks) {
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
                    Track track = new Track(songDuration, artist, title, path, id);
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

    static ArrayList<Track> search(ContentResolver contentResolver) {
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
                    Track track = new Track(songDuration, artist, title, path, id);
                    tracks.add(track);
                    id++;
                }
            }
            cursor.close();
        }
        return tracks;
    }
}
