package ru.krivocraft.kbmp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

class SQLiteProcessor {

    private final SQLiteDatabase db;

    SQLiteProcessor(Context context) {
        DBHelper dbHelper = new DBHelper(context);
        db = dbHelper.getWritableDatabase();
    }

    void writeComposition(Track track) {
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
                tracks.add(new Track(c.getString(compositionDurationColIndex),
                        c.getString(compositionAuthorColIndex),
                        c.getString(compositionNameColIndex),
                        c.getString(compositionPathColIndex),
                        c.getInt(compositionIdColIndex)));

            } while (c.moveToNext());
        }
        c.close();
        return tracks;
    }
}
