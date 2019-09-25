package ru.krivocraft.kbmp.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import ru.krivocraft.kbmp.Track;
import ru.krivocraft.kbmp.TrackList;
import ru.krivocraft.kbmp.TrackReference;

public class DBConnection {
    private SQLiteDatabase database;

    public DBConnection(Context context) {
        this.database = new DBHelper(context).getWritableDatabase();
    }

    public void writeTrack(Track track) {
        ContentValues values = new ContentValues();
        values.put("id", track.getIdentifier());
        values.put("duration", track.getDuration());
        values.put("title", track.getTitle());
        values.put("artist", track.getArtist());
        values.put("path", track.getPath());
        database.insert(TableNames.TRACKS, null, values);
    }

    public void updateTrack(Track track) {
        ContentValues values = new ContentValues();
        values.put("duration", track.getDuration());
        values.put("title", track.getTitle());
        values.put("artist", track.getArtist());
        values.put("path", track.getPath());
        database.update(TableNames.TRACKS, values, "id = ?", new String[]{String.valueOf(track.getIdentifier())});
    }

    public void removeTrackList(TrackList trackList) {

    }

    public void removeTrack(TrackReference track) {

    }

    public Track getTrack(TrackReference trackReference) {
        Track track = new Track(0, "", "", "");
        Cursor cursor = database.query(TableNames.TRACKS, null, "id = ?", new String[]{trackReference.toString()}, null, null, null);
        if (cursor.moveToFirst()) {

            long duration = cursor.getLong(cursor.getColumnIndex("duration"));
            String artist = cursor.getString(cursor.getColumnIndex("artist"));
            String title = cursor.getString(cursor.getColumnIndex("title"));
            String path = cursor.getString(cursor.getColumnIndex("path"));

            track = new Track(duration, artist, title, path);
        }
        cursor.close();
        return track;
    }

    public List<String> getTrackListIdentifiers() {
        return new ArrayList<>();
    }

    public void writeTrackList(TrackList trackList) {
        createTrackListTable(trackList);
        fillTrackListTable(trackList);
        createTrackListEntry(trackList);
    }

    public void updateTrackList(TrackList trackList) {
        ContentValues values = new ContentValues();
        values.put("name", trackList.getDisplayName());
        database.update(TableNames.TRACK_LISTS, values, "id = ?", new String[]{trackList.getIdentifier()});
    }

    public List<Track> getTracksStorage() {
        List<Track> tracks = new ArrayList<>();
        Cursor cursor = database.query(TableNames.TRACKS, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            int titleIndex = cursor.getColumnIndex("title");
            int artistIndex = cursor.getColumnIndex("artist");
            int pathIndex = cursor.getColumnIndex("path");
            int durationIndex = cursor.getColumnIndex("duration");
            do {
                long duration = cursor.getLong(durationIndex);
                String artist = cursor.getString(artistIndex);
                String title = cursor.getString(titleIndex);
                String path = cursor.getString(pathIndex);

                Track track = new Track(duration, artist, title, path);
                tracks.add(track);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return tracks;
    }

    public List<TrackList> getTrackLists() {
        List<TrackList> trackLists = new ArrayList<>();
        Cursor cursor = database.query(TableNames.TRACK_LISTS, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndex("title");
            int typeIndex = cursor.getColumnIndex("artist");
            do {
                int type = cursor.getInt(typeIndex);
                String displayName = cursor.getString(nameIndex);
                List<TrackReference> tracks = getTracksForTrackList(TrackList.createIdentifier(displayName));

                TrackList trackList = new TrackList(displayName, tracks, type);
                trackLists.add(trackList);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return trackLists;
    }

    private void createTrackListEntry(TrackList trackList) {
        ContentValues values = new ContentValues();
        values.put("id", trackList.getIdentifier());
        values.put("name", trackList.getDisplayName());
        values.put("type", trackList.getType());
        database.insert(TableNames.TRACK_LISTS, null, values);
    }

    private void fillTrackListTable(TrackList trackList) {
        ContentValues listItems = new ContentValues();
        for (TrackReference reference : trackList.getTrackReferences()) {
            listItems.put("reference", reference.getValue());
            database.insert(trackList.getIdentifier(), null, listItems);
        }
    }

    private void createTrackListTable(TrackList trackList) {
        database.execSQL("create table " + trackList.getIdentifier() + "("
                + "id integer primary key autoincrement,"
                + "reference integer);");
    }

    private List<TrackReference> getTracksForTrackList(String identifier) {
        List<TrackReference> trackReferences = new ArrayList<>();
        Cursor cursor = database.query(identifier, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            int valueIndex = cursor.getColumnIndex("reference");
            do {
                int value = cursor.getInt(valueIndex);

                TrackReference reference = new TrackReference(value);
                trackReferences.add(reference);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return trackReferences;
    }

}
