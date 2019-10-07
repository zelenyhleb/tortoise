package ru.krivocraft.kbmp.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import ru.krivocraft.kbmp.ColorManager;
import ru.krivocraft.kbmp.Track;
import ru.krivocraft.kbmp.TrackList;
import ru.krivocraft.kbmp.constants.Constants;

public class DBHelper extends SQLiteOpenHelper {

    private final ColorManager colorManager;

    DBHelper(@Nullable Context context) {
        super(context, "tracks", null, 2);
        this.colorManager = new ColorManager(context);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table if not exists " + TableNames.TRACKS + " ("
                + "id integer,"
                + "playing integer,"
                + "selected integer,"
                + "liked integer,"
                + "title text,"
                + "color integer,"
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
        try {
            db.execSQL("SELECT color FROM " + TableNames.TRACKS);
        } catch (Exception e) {
            db.execSQL("alter table " + TableNames.TRACKS + " add column color integer default " + ColorManager.GREEN);
            generateColors(db);
        }
    }

    private void updateColor(SQLiteDatabase database, Track track) {
        ContentValues values = new ContentValues();
        values.put("color", colorManager.getRandomColor());
        database.update(TableNames.TRACKS, values, "id = ?", new String[]{String.valueOf(track.getIdentifier())});
    }

    private void generateColors(SQLiteDatabase db) {
        Cursor cursor = db.query(TableNames.TRACKS, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            int titleIndex = cursor.getColumnIndex("title");
            int artistIndex = cursor.getColumnIndex("artist");
            int pathIndex = cursor.getColumnIndex("path");
            int durationIndex = cursor.getColumnIndex("duration");
            int colorIndex = cursor.getColumnIndex("color");
            int likedIndex = cursor.getColumnIndex("liked");
            int selectedIndex = cursor.getColumnIndex("selected");
            int playingIndex = cursor.getColumnIndex("playing");
            do {
                long duration = cursor.getLong(durationIndex);
                String artist = cursor.getString(artistIndex);
                String title = cursor.getString(titleIndex);
                String path = cursor.getString(pathIndex);
                boolean liked = cursor.getInt(likedIndex) == 1;
                boolean selected = cursor.getInt(selectedIndex) == 1;
                boolean playing = cursor.getInt(playingIndex) == 1;
                int color = cursor.getInt(colorIndex);

                Track track = new Track(duration, artist, title, path, liked, selected, playing, color);
                updateColor(db, track);
            } while (cursor.moveToNext());
        }
        cursor.close();
    }
}
