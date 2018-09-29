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

    SQLiteProcessor(Context context) {
        DBHelper dbHelper = new DBHelper(context);
        db = dbHelper.getWritableDatabase();
    }

    private void writeComposition(Track track) {
        if (!readCompositions().contains(track)) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(Constants.COMPOSITION_AUTHOR, track.getArtist());
            contentValues.put(Constants.COMPOSITION_NAME, track.getName());
            contentValues.put(Constants.COMPOSITION_PATH, track.getPath());
            contentValues.put(Constants.COMPOSITION_DURATION, track.getDuration());

            db.insert(Constants.COMPOSITIONS_LIST, null, contentValues);
        }

    }

    void writeCompositions(List<Track> tracks) {
        for (Track track : tracks) {
            writeComposition(track);
        }
    }

    void clearDatabase() {
        db.execSQL("delete from " + Constants.COMPOSITIONS_LIST);
    }

    List<Track> readCompositions() {
        Cursor c = db.query(Constants.COMPOSITIONS_LIST, null, null, null, null, null, Constants.COMPOSITION_IDENTIFIER);
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

    static class DBHelper extends SQLiteOpenHelper {

        DBHelper(Context context) {
            super(context, Constants.COMPOSITIONS_LIST, null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table " + Constants.COMPOSITIONS_LIST + " ("
                    + Constants.COMPOSITION_IDENTIFIER + " integer primary key autoincrement,"
                    + Constants.COMPOSITION_NAME + " text,"
                    + Constants.COMPOSITION_DURATION + " text,"
                    + Constants.COMPOSITION_AUTHOR + " text,"
                    + Constants.COMPOSITION_PATH + " text" + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}
