package ru.krivocraft.kbmp.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DBHelper extends SQLiteOpenHelper {

    DBHelper(@Nullable Context context) {
        super(context, "tracks", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table if not exists " + TableNames.TRACKS + " ("
                + "id integer,"
                + "title text,"
                + "artist text,"
                + "duration long,"
                + "path text);");
        db.execSQL("create table if not exists " + TableNames.TRACK_LISTS + " ("
                + "id text,"
                + "name text,"
                + "type integer);");
        db.execSQL("create table if not exists " + TableNames.TAGS + " ("
                + "id integer primary key autoincrement,"
                + "name text);");
        db.execSQL("create table if not exists " + TableNames.TAGS_TRACKS + " ("
                + "tag integer,"
                + "track integer);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
