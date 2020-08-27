/*
 * Copyright (c) 2020 Nikifor Fedorov
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
 *         Nikifor Fedorov and others
 */

package ru.krivocraft.tortoise.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;
import ru.krivocraft.tortoise.core.api.settings.ReadOnlySettings;
import ru.krivocraft.tortoise.core.model.Track;
import ru.krivocraft.tortoise.core.model.TrackList;
import ru.krivocraft.tortoise.core.model.TrackReference;
import ru.krivocraft.tortoise.core.model.track.TrackMeta;
import ru.krivocraft.tortoise.core.model.track.TrackPlayingState;
import ru.krivocraft.tortoise.core.settings.SettingsStorageManager;
import ru.krivocraft.tortoise.thumbnail.Colors;

import java.util.ArrayList;
import java.util.List;

public class DBConnection {
    private static final String TRACKS = "tracks";
    private static final String TRACK_LISTS = "track_lists";
    private static final String ALL_TRACKS = TrackList.createIdentifier(TrackList.STORAGE_TRACKS_DISPLAY_NAME);

    private final SQLiteDatabase database;
    private final ReadOnlySettings settings;

    public DBConnection(Context context, ReadOnlySettings settings) {
        this.database = new DBHelper(context).getWritableDatabase();
        this.settings = settings;
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
        values.put("rating", track.getRating());
        values.put("selected", track.isSelected() ? 1 : 0);
        values.put("ignored", track.isIgnored() ? 1 : 0);
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
        values.put("rating", track.getRating());
        values.put("playing", track.isPlaying() ? 1 : 0);
        values.put("liked", track.isLiked() ? 1 : 0);
        values.put("ignored", track.isIgnored() ? 1 : 0);
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
        Track track = new Track(new TrackMeta("", "", "", 0, Colors.GREEN), 0);
        Cursor cursor = database.query(TRACKS, null, "id = ?", new String[]{trackReference.toString()}, null, null, null);
        if (cursor.moveToFirst()) {

            int duration = cursor.getInt(cursor.getColumnIndex("duration"));
            String artist = cursor.getString(cursor.getColumnIndex("artist"));
            String title = cursor.getString(cursor.getColumnIndex("title"));
            String path = cursor.getString(cursor.getColumnIndex("path"));
            int color = cursor.getInt(cursor.getColumnIndex("color"));
            int rating = cursor.getInt(cursor.getColumnIndex("rating"));
            boolean liked = cursor.getInt(cursor.getColumnIndex("liked")) == 1;
            boolean selected = cursor.getInt(cursor.getColumnIndex("selected")) == 1;
            boolean playing = cursor.getInt(cursor.getColumnIndex("playing")) == 1;
            boolean ignored = cursor.getInt(cursor.getColumnIndex("ignored")) == 1;

            track = new Track(new TrackMeta(title, artist, path, duration, color), new TrackPlayingState(selected, playing), liked, ignored, rating);
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

    public TrackList getAllTracks() {
        return getTrackList(ALL_TRACKS);
    }

    public void clearTrackList(String trackList) {
        database.delete(trackList, "1", null);
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
            int ratingIndex = cursor.getColumnIndex("rating");
            int likedIndex = cursor.getColumnIndex("liked");
            int selectedIndex = cursor.getColumnIndex("selected");
            int playingIndex = cursor.getColumnIndex("playing");
            int ignoredIndex = cursor.getColumnIndex("ignored");
            do {
                int duration = cursor.getInt(durationIndex);
                String artist = cursor.getString(artistIndex);
                String title = cursor.getString(titleIndex);
                String path = cursor.getString(pathIndex);
                boolean liked = cursor.getInt(likedIndex) == 1;
                boolean selected = cursor.getInt(selectedIndex) == 1;
                boolean playing = cursor.getInt(playingIndex) == 1;
                boolean ignored = cursor.getInt(ignoredIndex) == 1;
                int color = cursor.getInt(colorIndex);
                int rating = cursor.getInt(ratingIndex);

                Track track = new Track(new TrackMeta(title, artist, path, duration, color), new TrackPlayingState(selected, playing), liked, ignored, rating);
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

    private TrackList getTrackList(String identifier) {
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
                    if (!getTrack(reference).isIgnored()) {
                        trackReferences.add(reference);
                    } else {
                        if (settings.read(SettingsStorageManager.KEY_SHOW_IGNORED, false)) {
                            trackReferences.add(reference);
                            System.out.println("adding ignored track " + getTrack(reference).getTitle());
                        } else {
                            System.out.println("not adding ignored");
                        }
                    }
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

        DBHelper(@Nullable Context context) {
            super(context, "tracks", null, 4);
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
                    + "rating integer,"
                    + "artist text,"
                    + "ignored integer,"
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
            values.put("id", TrackList.createIdentifier(TrackList.STORAGE_TRACKS_DISPLAY_NAME));
            values.put("name", TrackList.STORAGE_TRACKS_DISPLAY_NAME);
            values.put("type", TrackList.TRACK_LIST_CUSTOM);
            db.insert(TRACK_LISTS, null, values);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion < 2)
                try {
                    db.execSQL("SELECT color FROM " + TRACKS);
                } catch (Exception e) {
                    db.execSQL("alter table " + TRACKS + " add column color integer default " + Colors.GREEN);
                    generateColors(db);
                }
            if (oldVersion < 3)
                try {
                    db.execSQL("SELECT ignored FROM " + TRACKS);
                } catch (Exception e) {
                    db.execSQL("alter table " + TRACKS + " add column ignored integer default 0");
                }
            if (oldVersion < 4)
                try {
                    db.execSQL("SELECT rating FROM " + TRACKS);
                } catch (Exception e) {
                    db.execSQL("alter table " + TRACKS + " add column rating integer default 0");
                }
        }

        private void updateColor(SQLiteDatabase database, Track track) {
            ContentValues values = new ContentValues();
            values.put("color", Colors.getRandomColor());
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
                int ratingIndex = cursor.getColumnIndex("rating");
                int likedIndex = cursor.getColumnIndex("liked");
                int selectedIndex = cursor.getColumnIndex("selected");
                int playingIndex = cursor.getColumnIndex("playing");
                int ignoredIndex = cursor.getColumnIndex("ignored");
                do {
                    int duration = cursor.getInt(durationIndex);
                    String artist = cursor.getString(artistIndex);
                    String title = cursor.getString(titleIndex);
                    String path = cursor.getString(pathIndex);
                    int rating = cursor.getInt(ratingIndex);
                    boolean liked = cursor.getInt(likedIndex) == 1;
                    boolean selected = cursor.getInt(selectedIndex) == 1;
                    boolean playing = cursor.getInt(playingIndex) == 1;
                    boolean ignored = cursor.getInt(ignoredIndex) == 1;
                    int color = cursor.getInt(colorIndex);

                    Track track = new Track(new TrackMeta(title, artist, path, duration, color), new TrackPlayingState(selected, playing), liked, ignored, rating);
                    updateColor(db, track);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
    }
}
