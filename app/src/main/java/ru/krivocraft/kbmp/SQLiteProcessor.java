package ru.krivocraft.kbmp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class SQLiteProcessor {

    private final DBHelper dbHelper;
    private final SQLiteDatabase db;

    SQLiteProcessor(Context context) {
        dbHelper = new DBHelper(context);

        db = dbHelper.getWritableDatabase();
    }

    void writeComposition(Composition composition) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(Constants.COMPOSITION_IDENTIFIER, String.valueOf(composition.getIdentifier()));
        contentValues.put(Constants.COMPOSITION_AUTHOR, composition.getAuthor());
        contentValues.put(Constants.COMPOSITION_NAME, composition.getName());
        contentValues.put(Constants.COMPOSITION_PATH, composition.getPath());
        contentValues.put(Constants.COMPOSITION_DURATION, composition.getDuration());

        db.insert(Constants.COMPOSITIONS_LIST, null, contentValues);
    }

    List<Composition> readCompositions() {
        Cursor c = db.query(Constants.COMPOSITIONS_LIST, null, null, null, null, null, Constants.COMPOSITION_IDENTIFIER);
        List<Composition> compositions = new ArrayList<>();
        if (c.moveToFirst()) {
            int compositionIdColIndex = c.getColumnIndex(Constants.COMPOSITION_IDENTIFIER);
            int compositionAuthorColIndex = c.getColumnIndex(Constants.COMPOSITION_AUTHOR);
            int compositionNameColIndex = c.getColumnIndex(Constants.COMPOSITION_NAME);
            int compositionPathColIndex = c.getColumnIndex(Constants.COMPOSITION_PATH);
            int compositionDurationColIndex = c.getColumnIndex(Constants.COMPOSITION_DURATION);

            do {
                compositions.add(new Composition(c.getString(compositionDurationColIndex),
                        c.getString(compositionAuthorColIndex),
                        c.getString(compositionNameColIndex),
                        c.getString(compositionPathColIndex),
                        c.getInt(compositionIdColIndex)));

            } while (c.moveToNext());
        }
        c.close();
        return compositions;
    }
}
