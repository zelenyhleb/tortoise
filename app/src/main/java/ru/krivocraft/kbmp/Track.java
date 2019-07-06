package ru.krivocraft.kbmp;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaMetadataCompat;

import java.util.Objects;

class Track implements Parcelable {

    private boolean playing = false;
    private boolean selected = false;
    private boolean checked = false;

    private MediaMetadataCompat metadata;

    //This constructor is used by search util - to describe new track entity from file on disk
    Track(@NonNull String duration, String artist, String title, @NonNull String path) {

        if (title != null) {
            String[] meta = title.split(" - ");
            if (meta.length > 1) {
                artist = meta[0];
                title = meta[1];
            } else {
                if (artist.equals("<unknown>")) {
                    artist = Constants.UNKNOWN_ARTIST;
                }
                if (title.equals("<unknown>")) {
                    title = Constants.UNKNOWN_COMPOSITION;
                }
            }
        }

        buildMediaMetadata(duration, artist, title, path);

    }

    Track(MediaMetadataCompat metadata) {
        this.metadata = metadata;
    }

    protected Track(Parcel in) {
        playing = in.readByte() != 0;
        selected = in.readByte() != 0;
        checked = in.readByte() != 0;
        metadata = in.readParcelable(MediaMetadataCompat.class.getClassLoader());
    }

    public static final Creator<Track> CREATOR = new Creator<Track>() {
        @Override
        public Track createFromParcel(Parcel in) {
            return new Track(in);
        }

        @Override
        public Track[] newArray(int size) {
            return new Track[size];
        }
    };

    MediaMetadataCompat getAsMediaMetadata() {
        return metadata;
    }

    private void buildMediaMetadata(@NonNull String duration, String artist, String title, @NonNull String path) {
        metadata = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, path)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, Long.parseLong(duration))
                .build();
    }

    boolean isSelected() {
        return selected;
    }

    void setSelected(boolean selected) {
        this.selected = selected;
    }

    String getIdentifier() {
        return metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI);
    }

    boolean isPlaying() {
        return playing;
    }

    void setPlaying(boolean playing) {
        this.playing = playing;
    }

    boolean isChecked() {
        return checked;
    }

    void setChecked(boolean checked) {
        this.checked = checked;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Track track = (Track) o;
        return Objects.equals(getIdentifier(), track.getIdentifier()) &&
                Objects.equals(getArtist(), track.getArtist()) &&
                Objects.equals(getTitle(), track.getTitle()) &&
                Objects.equals(getPath(), track.getPath());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getArtist(), getTitle(), getPath(), getIdentifier());
    }

    String getArtist() {
        return metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
    }

    String getTitle() {
        return metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE);
    }

    String getPath() {
        return metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI);
    }

    Bitmap getArt() {
        return metadata.getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART);
    }

    long getDuration() {
        return metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (playing ? 1 : 0));
        dest.writeByte((byte) (selected ? 1 : 0));
        dest.writeByte((byte) (checked ? 1 : 0));
        dest.writeParcelable(metadata, flags);
    }
}
