package ru.krivocraft.kbmp.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import ru.krivocraft.kbmp.Tag;
import ru.krivocraft.kbmp.Track;
import ru.krivocraft.kbmp.TrackList;
import ru.krivocraft.kbmp.TrackReference;

public class DBConnection {
    private SQLiteDatabase database;

    public DBConnection(Context context) {
        this.database = new DBHelper(context).getWritableDatabase();
    }

    public void writeTrack(Track track) {
        ContentValues values = new ContentValues();
        values.put("id", track.getIdentifier());
        values.put("duration", track.getDuration());
        values.put("title", track.getTitle());
        values.put("playing", track.isPlaying() ? 1 : 0);
        values.put("liked", track.isLiked() ? 1 : 0);
        values.put("selected", track.isSelected() ? 1 : 0);
        values.put("artist", track.getArtist());
        values.put("path", track.getPath());
        database.insert(TableNames.TRACKS, null, values);
    }

    public void updateTrack(Track track) {
        ContentValues values = new ContentValues();
        values.put("duration", track.getDuration());
        values.put("title", track.getTitle());
        values.put("artist", track.getArtist());
        values.put("path", track.getPath());
        values.put("playing", track.isPlaying() ? 1 : 0);
        values.put("liked", track.isLiked() ? 1 : 0);
        values.put("selected", track.isSelected() ? 1 : 0);
        database.update(TableNames.TRACKS, values, "id = ?", new String[]{String.valueOf(track.getIdentifier())});

        for (Tag tag : getTagsForTrack(track)) {
            if (!track.getTags().contains(tag)) {
                unbindTagFromTrack(tag, track);
            }
        }
        for (Tag tag : track.getTags()) {
            bindTag(tag, track);
        }
    }

    public void removeTrackList(TrackList trackList) {
        database.delete(TableNames.TRACK_LISTS, "id = ?", new String[]{trackList.getIdentifier()});
        database.execSQL("drop table if exists '" + trackList.getIdentifier() + "'");
    }

    public void removeTrack(Track track) {
        database.delete(TableNames.TRACKS, "id = ?", new String[]{String.valueOf(track.getIdentifier())});
        for (Tag tag : track.getTags()) {
            unbindTagFromTrack(tag, track);
        }
    }

    public Track getTrack(TrackReference trackReference) {
        Track track = new Track(0, "", "", "");
        Cursor cursor = database.query(TableNames.TRACKS, null, "id = ?", new String[]{trackReference.toString()}, null, null, null);
        if (cursor.moveToFirst()) {

            long duration = cursor.getLong(cursor.getColumnIndex("duration"));
            String artist = cursor.getString(cursor.getColumnIndex("artist"));
            String title = cursor.getString(cursor.getColumnIndex("title"));
            String path = cursor.getString(cursor.getColumnIndex("path"));
            boolean liked = cursor.getInt(cursor.getColumnIndex("liked")) == 1;
            boolean selected = cursor.getInt(cursor.getColumnIndex("selected")) == 1;
            boolean playing = cursor.getInt(cursor.getColumnIndex("playing")) == 1;

            track = new Track(duration, artist, title, path, liked, selected, playing);
            track.addTags(getTagsForTrack(track));
        }
        cursor.close();
        return track;
    }

    public List<String> getTrackListNames() {
        List<String> identifiers = new ArrayList<>();
        Cursor cursor = database.query(TableNames.TRACK_LISTS, new String[]{"name"}, null, null, null, null, null);
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

    public void clearTrackList(TrackList trackList) {
        database.delete(trackList.getIdentifier(), "1", null);
    }

    public void updateTrackList(TrackList trackList) {
        ContentValues values = new ContentValues();
        values.put("name", trackList.getDisplayName());
        database.update(TableNames.TRACK_LISTS, values, "id = ?", new String[]{trackList.getIdentifier()});
        database.execSQL("delete from " + trackList.getIdentifier());
        fillTrackListTable(trackList);
    }

    public void updateRootTrackList(TrackList trackList) {
        fillTrackListTable(trackList);
    }

    public List<Track> getTracksStorage() {
        List<Track> tracks = new ArrayList<>();
        Cursor cursor = database.query(TableNames.TRACKS, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            int titleIndex = cursor.getColumnIndex("title");
            int artistIndex = cursor.getColumnIndex("artist");
            int pathIndex = cursor.getColumnIndex("path");
            int durationIndex = cursor.getColumnIndex("duration");
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

                Track track = new Track(duration, artist, title, path, liked, selected, playing);
                track.addTags(getTagsForTrack(track));
                tracks.add(track);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return tracks;
    }

    public List<TrackList> getTrackLists() {
        List<TrackList> trackLists = new ArrayList<>();
        Cursor cursor = database.query(TableNames.TRACK_LISTS, null, null, null, null, null, null);
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
        Cursor cursor = database.query(TableNames.TRACK_LISTS, null, "id = ?", new String[]{identifier}, null, null, null);
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
            database.insert(TableNames.TRACK_LISTS, null, values);
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

    public List<Tag> getTagsForTrack(Track track) {
        List<Tag> tags = new ArrayList<>();
        Cursor cursor = database.query(
                TableNames.TAGS_TRACKS,
                new String[]{"tag"}, "track = ?",
                new String[]{String.valueOf(track.getIdentifier())},
                null, null, null);
        if (cursor.moveToFirst()) {
            do {
                tags.add(getTag(cursor.getInt(cursor.getColumnIndex("tag"))));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return tags;
    }

    public List<Tag> getTags() {
        List<Tag> tags = new ArrayList<>();
        Cursor cursor = database.query(TableNames.TAGS, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                tags.add(new Tag(cursor.getString(cursor.getColumnIndex("name"))));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return tags;
    }

    public void bindTag(Tag tag, Track track) {
        if (!getTags().contains(tag)) {
            writeTag(tag);
        }

        if (!getTagsForTrack(track).contains(tag)) {
            int tagId = getTagId(tag);
            if (tagId > -1) {
                ContentValues values = new ContentValues();
                values.put("tag", tagId);
                values.put("track", track.getIdentifier());
                database.insert(TableNames.TAGS_TRACKS, null, values);
            }
        }
    }

    private int getTagId(Tag tag) {
        int id = -1;
        Cursor cursor = database.query(TableNames.TAGS, new String[]{"id"}, "name = ?", new String[]{tag.getText()}, null, null, null);
        if (cursor.moveToFirst()) {
            id = cursor.getInt(cursor.getColumnIndex("id"));
        }
        cursor.close();
        return id;
    }

    public void unbindTagFromTrack(Tag tag, Track track) {
        database.delete(TableNames.TAGS_TRACKS, "tag = ? and track = ?", new String[]{String.valueOf(getTagId(tag)), String.valueOf(track.getIdentifier())});
    }

    private void writeTag(Tag tag) {
        ContentValues values = new ContentValues();
        values.put("name", tag.getText());
        database.insert(TableNames.TAGS, null, values);
    }

    private Tag getTag(int index) {
        Tag tag = null;
        Cursor cursor = database.query(TableNames.TAGS, null, "id = ?", new String[]{String.valueOf(index)}, null, null, null);
        if (cursor.moveToFirst()) {
            tag = new Tag(cursor.getString(cursor.getColumnIndex("name")));
        }
        cursor.close();
        return tag;
    }

    private void createTrackListTable(TrackList trackList) {
        database.execSQL("create table if not exists " + trackList.getIdentifier() + " ("
                + "id integer primary key autoincrement, "
                + "reference integer);");
    }

    private List<TrackReference> getTracksForTrackList(String identifier) {
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
    }

}
