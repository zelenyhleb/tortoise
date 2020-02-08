/*
 * Copyright (c) 2019 Nikifor Fedorov
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *     SPDX-License-Identifier: Apache-2.0
 *     Contributors:
 * 	    Nikifor Fedorov - whole development
 */

package ru.krivocraft.tortoise.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;
import ru.krivocraft.tortoise.core.ColorManager;
import ru.krivocraft.tortoise.core.storage.TrackListsStorageManager;
import ru.krivocraft.tortoise.core.track.Track;
import ru.krivocraft.tortoise.core.track.TrackList;
import ru.krivocraft.tortoise.core.track.TrackReference;

import java.util.ArrayList;
import java.util.List;

public class DBConnection {
    private static final String TRACKS = "tracks";
    private static final String TRACK_LISTS = "track_lists";
    private static final String ALL_TRACKS = TrackList.createIdentifier(TrackListsStorageManager.STORAGE_TRACKS_DISPLAY_NAME);
    private SQLiteDatabase database;

    public DBConnection(Context context) {
        this.database = new DBHelper(context).getWritableDatabase();
        removeDuplicatedTrackLists();
    }

    public void writeTrack(Track track) {
        ContentValues values = new ContentValues();
        values.put("id", track.getIdentifier());
        values.put("duration", track.getDuration());
        values.put("title", track.getTitle());
        values.put("playing", track.isPlaying() ? 1 : 0);
        values.put("liked", track.isLiked() ? 1 : 0);
        values.put("color", track.getColor());
        values.put("selected", track.isSelected() ? 1 : 0);
        values.put("artist", track.getArtist());
        values.put("path", track.getPath());
        database.insert(TRACKS, null, values);
    }

    public void updateTrack(Track track) {
        ContentValues values = new ContentValues();
        values.put("duration", track.getDuration());
        values.put("title", track.getTitle());
        values.put("artist", track.getArtist());
        values.put("path", track.getPath());
        values.put("color", track.getColor());
        values.put("playing", track.isPlaying() ? 1 : 0);
        values.put("liked", track.isLiked() ? 1 : 0);
        values.put("selected", track.isSelected() ? 1 : 0);
        database.update(TRACKS, values, "id = ?", new String[]{String.valueOf(track.getIdentifier())});
    }

    public void removeTrackList(TrackList trackList) {
        database.delete(TRACK_LISTS, "id = ?", new String[]{trackList.getIdentifier()});
        database.execSQL("drop table if exists '" + trackList.getIdentifier() + "'");
    }

    public void removeTrack(Track track) {
        database.delete(TRACKS, "id = ?", new String[]{String.valueOf(track.getIdentifier())});
    }

    public Track getTrack(TrackReference trackReference) {
        Track track = new Track(0, "", "", "", ColorManager.GREEN);
        Cursor cursor = database.query(TRACKS, null, "id = ?", new String[]{trackReference.toString()}, null, null, null);
        if (cursor.moveToFirst()) {

            long duration = cursor.getLong(cursor.getColumnIndex("duration"));
            String artist = cursor.getString(cursor.getColumnIndex("artist"));
            String title = cursor.getString(cursor.getColumnIndex("title"));
            String path = cursor.getString(cursor.getColumnIndex("path"));
            int color = cursor.getInt(cursor.getColumnIndex("color"));
            boolean liked = cursor.getInt(cursor.getColumnIndex("liked")) == 1;
            boolean selected = cursor.getInt(cursor.getColumnIndex("selected")) == 1;
            boolean playing = cursor.getInt(cursor.getColumnIndex("playing")) == 1;

            track = new Track(duration, artist, title, path, liked, selected, playing, color);
        }
        cursor.close();
        return track;
    }

    public List<String> getTrackListNames() {
        List<String> identifiers = new ArrayList<>();
        Cursor cursor = database.query(TRACK_LISTS, new String[]{"name"}, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                identifiers.add(cursor.getString(cursor.getColumnIndex("name")));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return identifiers;
    }

    public void writeTrackList(TrackList trackList) {
        createTrackListTable(trackList);
        fillTrackListTable(trackList);
        createTrackListEntry(trackList);
    }

    private void removeDuplicatedTrackLists() {
        List<String> trackListNames = getTrackListNames();
        for (String string : trackListNames) {
            Cursor cursor = database.query(TRACK_LISTS, null, "name = ?", new String[]{string}, null, null, null);
            if (cursor.moveToFirst()) {
                String identifier = cursor.getString(cursor.getColumnIndex("id"));
                int type = cursor.getInt(cursor.getColumnIndex("type"));

                database.delete(TRACK_LISTS, "name = ?", new String[]{string});
                ContentValues values = new ContentValues();
                values.put("id", identifier);
                values.put("type", type);
                values.put("name", string);
                database.insert(TRACK_LISTS, null, values);
            }
            cursor.close();
        }
    }

    public void clearTrackList(String trackList) {
        database.delete(trackList, "1", null);
    }

    public void updateTrackListData(TrackList trackList) {
        ContentValues values = new ContentValues();
        values.put("name", trackList.getDisplayName());
        database.update(TRACK_LISTS, values, "id = ?", new String[]{trackList.getIdentifier()});
    }

    public void updateTrackListContent(TrackList trackList) {
        clearTrackList(trackList.getIdentifier());
        fillTrackListTable(trackList);
    }

    public void updateRootTrackList(TrackList trackList) {
        fillTrackListTable(trackList);
    }

    public List<Track> getTracksStorage() {
        List<Track> tracks = new ArrayList<>();
        Cursor cursor = database.query(TRACKS, null, null, null, null, null, null);
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
                tracks.add(track);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return tracks;
    }

    public List<TrackList> getTrackLists() {
        List<TrackList> trackLists = new ArrayList<>();
        Cursor cursor = database.query(TRACK_LISTS, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex("id");
            int nameIndex = cursor.getColumnIndex("name");
            int typeIndex = cursor.getColumnIndex("type");
            do {
                int type = cursor.getInt(typeIndex);
                String displayName = cursor.getString(nameIndex);
                String identifier = cursor.getString(idIndex);
                List<TrackReference> tracks = getTracksForTrackList(identifier);

                TrackList trackList = new TrackList(displayName, tracks, type, identifier);
                trackLists.add(trackList);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return trackLists;
    }

    public List<TrackList> getCustom() {
        return getFilteredTrackLists(TrackList.TRACK_LIST_CUSTOM);
    }

    public List<TrackList> getSortedByArtist() {
        return getFilteredTrackLists(TrackList.TRACK_LIST_BY_AUTHOR);
    }

    private List<TrackList> getFilteredTrackLists(int filter) {
        List<TrackList> trackLists = new ArrayList<>();

        Cursor cursor = database.query(TRACK_LISTS, null,
                "type == " + filter,
                null, null, null, null);
        if (cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex("id");
            int nameIndex = cursor.getColumnIndex("name");
            int typeIndex = cursor.getColumnIndex("type");
            do {
                int type = cursor.getInt(typeIndex);
                String displayName = cursor.getString(nameIndex);
                String identifier = cursor.getString(idIndex);
                List<TrackReference> tracks = getTracksForTrackList(identifier);

                TrackList trackList = new TrackList(displayName, tracks, type, identifier);
                trackLists.add(trackList);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return trackLists;
    }

    public TrackList getTrackList(String identifier) {
        TrackList trackList = null;
        Cursor cursor = database.query(TRACK_LISTS, null, "id = ?", new String[]{identifier}, null, null, null);
        if (cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex("id");
            int nameIndex = cursor.getColumnIndex("name");
            int typeIndex = cursor.getColumnIndex("type");
            do {
                int type = cursor.getInt(typeIndex);
                String displayName = cursor.getString(nameIndex);
                List<TrackReference> tracks = getTracksForTrackList(cursor.getString(idIndex));

                trackList = new TrackList(displayName, tracks, type, cursor.getString(idIndex));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return trackList;
    }

    private void createTrackListEntry(TrackList trackList) {
        if (getTrackList(trackList.getIdentifier()) == null) {
            ContentValues values = new ContentValues();
            values.put("id", trackList.getIdentifier());
            values.put("name", trackList.getDisplayName());
            values.put("type", trackList.getType());
            database.insert(TRACK_LISTS, null, values);
        }
    }

    private void fillTrackListTable(TrackList trackList) {
        for (TrackReference reference : trackList.getTrackReferences()) {
            ContentValues listItems = new ContentValues();
            listItems.put("reference", reference.getValue());
            database.insert(trackList.getIdentifier(), null, listItems);
        }
    }

    public void removeTracks(TrackList trackList, List<TrackReference> references) {
        for (TrackReference reference : references) {
            database.delete(trackList.getIdentifier(), "reference = ?", new String[]{reference.toString()});
        }
    }

    private void createTrackListTable(TrackList trackList) {
        database.execSQL("create table if not exists " + trackList.getIdentifier() + " ("
                + "id integer primary key autoincrement, "
                + "reference integer);");
    }

    private List<TrackReference> getTracksForTrackList(String identifier) {
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static class DBHelper extends SQLiteOpenHelper {

        private final ColorManager colorManager;

        DBHelper(@Nullable Context context) {
            super(context, "tracks", null, 2);
            this.colorManager = new ColorManager(context);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table if not exists " + TRACKS + " ("
                    + "id integer,"
                    + "playing integer,"
                    + "selected integer,"
                    + "liked integer,"
                    + "title text,"
                    + "color integer,"
                    + "artist text,"
                    + "duration long,"
                    + "path text);");
            db.execSQL("create table if not exists " + TRACK_LISTS + " ("
                    + "id text,"
                    + "name text,"
                    + "type integer);");
            db.execSQL("create table if not exists " + ALL_TRACKS + " ("
                    + "id integer primary key autoincrement,"
                    + "reference integer);");

            ContentValues values = new ContentValues();
            values.put("id", TrackList.createIdentifier(TrackListsStorageManager.STORAGE_TRACKS_DISPLAY_NAME));
            values.put("name", TrackListsStorageManager.STORAGE_TRACKS_DISPLAY_NAME);
            values.put("type", TrackList.TRACK_LIST_CUSTOM);
            db.insert(TRACK_LISTS, null, values);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            try {
                db.execSQL("SELECT color FROM " + TRACKS);
            } catch (Exception e) {
                db.execSQL("alter table " + TRACKS + " add column color integer default " + ColorManager.GREEN);
                generateColors(db);
            }
        }

        private void updateColor(SQLiteDatabase database, Track track) {
            ContentValues values = new ContentValues();
            values.put("color", colorManager.getRandomColor());
            database.update(TRACKS, values, "id = ?", new String[]{String.valueOf(track.getIdentifier())});
        }

        private void generateColors(SQLiteDatabase db) {
            Cursor cursor = db.query(TRACKS, null, null, null, null, null, null);
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
}
