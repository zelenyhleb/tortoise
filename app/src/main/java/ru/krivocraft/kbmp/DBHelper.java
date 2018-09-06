package ru.krivocraft.kbmp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context) {
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
