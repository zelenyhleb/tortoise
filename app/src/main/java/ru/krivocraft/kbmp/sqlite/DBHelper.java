package ru.krivocraft.kbmp.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import ru.krivocraft.kbmp.TrackList;
import ru.krivocraft.kbmp.constants.Constants;

public class DBHelper extends SQLiteOpenHelper {

    DBHelper(@Nullable Context context) {
        super(context, "tracks", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table if not exists " + TableNames.TRACKS + " ("
                + "id integer,"
                + "playing integer,"
                + "selected integer,"
                + "liked integer,"
                + "title text,"
                + "artist text,"
                + "duration long,"
                + "path text);");
        db.execSQL("create table if not exists " + TableNames.TRACK_LISTS + " ("
                + "id text,"
                + "name text,"
                + "type integer);");
        db.execSQL("create table if not exists " + TableNames.ALL_TRACKS + " ("
                + "id integer primary key autoincrement,"
                + "reference integer);");

        ContentValues values = new ContentValues();
        values.put("id", TrackList.createIdentifier(Constants.STORAGE_TRACKS_DISPLAY_NAME));
        values.put("name", Constants.STORAGE_TRACKS_DISPLAY_NAME);
        values.put("type", Constants.TRACK_LIST_CUSTOM);
        db.insert(TableNames.TRACK_LISTS, null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
