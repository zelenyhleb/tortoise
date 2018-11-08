package ru.krivocraft.kbmp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

class SQLiteProcessor {

    private final SQLiteDatabase db;
    private final Context context;

    SQLiteProcessor(Context context) {
        this.context = context;
        DBHelper dbHelper = new DBHelper(context);
        db = dbHelper.getWritableDatabase();
        db.execSQL("create table if not exists " + Constants.COMPOSITIONS + " ("
                + Constants.COMPOSITION_IDENTIFIER + " integer primary key autoincrement, "
                + Constants.COMPOSITION_NAME + " text,"
                + Constants.COMPOSITION_DURATION + " text,"
                + Constants.COMPOSITION_AUTHOR + " text,"
                + Constants.COMPOSITION_PATH + " text" + ");");
    }

    private void writeComposition(Track track) {
        if (!readCompositions().contains(track)) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(Constants.COMPOSITION_AUTHOR, track.getArtist());
            contentValues.put(Constants.COMPOSITION_NAME, track.getName());
            contentValues.put(Constants.COMPOSITION_PATH, track.getPath());
            contentValues.put(Constants.COMPOSITION_DURATION, track.getDuration());

            db.insert(Constants.COMPOSITIONS, null, contentValues);
        }

    }

    void writeCompositions(List<Track> tracks) {
        for (Track track : tracks) {
            writeComposition(track);
        }
    }

    void clearDatabase() {
        db.execSQL("drop table " + Constants.COMPOSITIONS);
    }

    List<Track> readCompositions() {
        Cursor c = db.query(Constants.COMPOSITIONS, null, null, null, null, null, Constants.COMPOSITION_IDENTIFIER);
        List<Track> tracks = new ArrayList<>();
        if (c.moveToFirst()) {
            int compositionIdColIndex = c.getColumnIndex(Constants.COMPOSITION_IDENTIFIER);
            int compositionAuthorColIndex = c.getColumnIndex(Constants.COMPOSITION_AUTHOR);
            int compositionNameColIndex = c.getColumnIndex(Constants.COMPOSITION_NAME);
            int compositionPathColIndex = c.getColumnIndex(Constants.COMPOSITION_PATH);
            int compositionDurationColIndex = c.getColumnIndex(Constants.COMPOSITION_DURATION);

            do {
                String duration = c.getString(compositionDurationColIndex);
                String author = c.getString(compositionAuthorColIndex);
                String name = c.getString(compositionNameColIndex);
                String path = c.getString(compositionPathColIndex);
                int id = c.getInt(compositionIdColIndex);
                tracks.add(new Track(duration, author, name, path, id));

            } while (c.moveToNext());
        }
        c.close();
        return tracks;
    }

    Track readComposition(int requestedId) {
        Cursor c = db.rawQuery("select * from " + Constants.COMPOSITIONS + " where '" + Constants.COMPOSITION_IDENTIFIER + "' = " + requestedId, null);
        Track track = null;
        if (c.moveToFirst()) {
            int compositionIdColIndex = c.getColumnIndex(Constants.COMPOSITION_IDENTIFIER);
            int compositionAuthorColIndex = c.getColumnIndex(Constants.COMPOSITION_AUTHOR);
            int compositionNameColIndex = c.getColumnIndex(Constants.COMPOSITION_NAME);
            int compositionPathColIndex = c.getColumnIndex(Constants.COMPOSITION_PATH);
            int compositionDurationColIndex = c.getColumnIndex(Constants.COMPOSITION_DURATION);

            String duration = c.getString(compositionDurationColIndex);
            String author = c.getString(compositionAuthorColIndex);
            String name = c.getString(compositionNameColIndex);
            String path = c.getString(compositionPathColIndex);
            int id = c.getInt(compositionIdColIndex);
            track = new Track(duration, author, name, path, id);

        }
        c.close();
        return track;
    }

    List<Playlist> getPlaylists() {
        Cursor co = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        List<Playlist> playlists = new ArrayList<>();

        if (co.moveToFirst()) {
            while (!co.isAfterLast()) { //for each playlist

                String tableName = co.getString(0);
                if (tableName.contains("playlist")) {
                    Cursor ci = db.query(tableName, null,null,null,null, null, Constants.PLAYLIST_INDEX);
                    if (ci.moveToFirst()) {
                        int playlistTrackReferenceIndex = ci.getColumnIndex(Constants.PLAYLIST_COMPOSITION_REFERENCE);
                        Playlist playlist = new Playlist(context);

                        while (!ci.isAfterLast()) { //for each track in playlist
                            playlist.addComposition(readComposition(ci.getInt(playlistTrackReferenceIndex)));
                            ci.moveToNext();
                        }

                        playlists.add(playlist);
                    }
                    ci.close();
                }

                co.moveToNext();
            }
        }
        co.close();
        return playlists;
    }

    void createPlaylist(String name) {
        db.execSQL("create table if not exists " + name + " ("
                + Constants.PLAYLIST_INDEX + " integer primary key autoincrement, "
                + Constants.PLAYLIST_COMPOSITION_REFERENCE + " integer);");
    }

    void editPlaylist(String name, List<Integer> ids) {
        for (Integer integer : ids) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(Constants.PLAYLIST_INDEX, "null");
            contentValues.put(Constants.PLAYLIST_COMPOSITION_REFERENCE, integer);
            db.insert(name, null, contentValues);
        }
    }

    void deletePlaylist(String name) {
        db.execSQL("drop table " + name);
    }

    Playlist getPlaylist() {
        return null;
    }

    static class DBHelper extends SQLiteOpenHelper {

        DBHelper(Context context) {
            super(context, Constants.COMPOSITIONS, null, 1);

        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}
