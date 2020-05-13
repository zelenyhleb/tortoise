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

package ru.krivocraft.tortoise.core.model;

import android.support.v4.media.MediaMetadataCompat;
import androidx.annotation.NonNull;

import java.util.Objects;

public class Track {

    public static final String EXTRA_TRACK = "path";

    private static final String UNKNOWN_ARTIST = "Unknown Artist";
    private static final String UNKNOWN_COMPOSITION = "Unknown Track";

    private boolean selected = false;
    private boolean playing = false;
    private boolean liked = false;
    private boolean checkedInList = false;
    private boolean ignored = false;

    private String title;
    private String artist;
    private String path;
    private long duration;
    private int identifier;
    private int color;
    private int rating;

    public Track(long duration, String artist, String title, @NonNull String path, int color, int rating) {
        this.duration = duration;
        this.artist = artist;
        this.title = title;
        this.path = path;
        this.color = color;

        this.identifier = path.hashCode();
        this.rating = rating;

        if ("<unknown>".equals(artist)) {
            this.artist = UNKNOWN_ARTIST;
        }
        if ("<unknown>".equals(title)) {
            this.title = UNKNOWN_COMPOSITION;
        }
    }

    public Track(long duration, String artist, String title, String path, boolean liked, boolean selected, boolean playing, int color, boolean ignored, int rating) {
        this(duration, artist, title, path, color, rating);
        this.liked = liked;
        this.selected = selected;
        this.playing = playing;
        this.ignored = ignored;
    }

    public MediaMetadataCompat getAsMediaMetadata() {
        return new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, path)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
                .build();
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public boolean isCheckedInList() {
        return checkedInList;
    }

    public void setCheckedInList(boolean checkedInList) {
        this.checkedInList = checkedInList;
    }

    public void setIgnored(boolean ignored) {
        this.ignored = ignored;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Track track = (Track) o;
        return Objects.equals(getArtist(), track.getArtist()) &&
                Objects.equals(getTitle(), track.getTitle()) &&
                Objects.equals(getPath(), track.getPath());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getArtist(), getTitle(), getPath());
    }

    public String getArtist() {
        return artist;
    }

    public String getTitle() {
        return title;
    }

    public String getPath() {
        return path;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public int getIdentifier() {
        return identifier;
    }

    public long getDuration() {
        return duration;
    }

    public int getColor() {
        return color;
    }

    public boolean isIgnored() {
        return ignored;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }
}
