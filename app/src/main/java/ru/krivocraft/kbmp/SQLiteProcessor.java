package ru.krivocraft.kbmp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

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

        db.insert(Constants.COMPOSITIONS_LIST, null, contentValues);
    }

    void readCompositions() {
        Cursor c = db.query(Constants.COMPOSITIONS_LIST, null, null, null, null, null, Constants.COMPOSITION_IDENTIFIER);
        if (c.moveToFirst()) {
            int idColIndex = c.getColumnIndex(Constants.COMPOSITION_IDENTIFIER);
            int compositionAuthorColIndex = c.getColumnIndex(Constants.COMPOSITION_AUTHOR);
            int compositionNameColIndex = c.getColumnIndex(Constants.COMPOSITION_NAME);
            int compositionPathColIndex = c.getColumnIndex(Constants.COMPOSITION_PATH);

            do {
                // получаем значения по номерам столбцов и пишем все в лог
//                Log.d(LOG_TAG,
//                        "ID = " + c.getInt(idColIndex) +
//                                ", name = " + c.getString(compositionNameColIndex) +
//                                ", email = " + c.getString(compositionPathColIndex));
                // переход на следующую строку
                // а если следующей нет (текущая - последняя), то false - выходим из цикла
            } while (c.moveToNext());
        }
        c.close();
    }
}
