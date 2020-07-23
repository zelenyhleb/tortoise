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
import ru.krivocraft.tortoise.core.model.track.TrackMeta;
import ru.krivocraft.tortoise.core.model.track.TrackPlayingState;

import java.util.Objects;

public class Track {

    public static final String EXTRA_TRACK = "path";

    private boolean selected = false;
    private boolean playing = false;
    private boolean liked = false;
    private boolean checkedInList = false;
    private boolean ignored = false;
    private TrackMeta trackMeta;

    private final int identifier;
    private int rating;

    public Track(TrackMeta trackMeta, int rating) {
        this.trackMeta = trackMeta;

        this.identifier = trackMeta.getPath().hashCode();
        this.rating = rating;
    }

    public Track(TrackMeta trackMeta, TrackPlayingState playingState, boolean liked, boolean ignored, int rating) {
        this(trackMeta, rating);
        this.liked = liked;
        this.selected = playingState.isSelected();
        this.playing = playingState.isPlaying();
        this.ignored = ignored;
    }

    public MediaMetadataCompat getAsMediaMetadata() {
        return new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, getArtist())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, getTitle())
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, getPath())
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, getDuration())
                .build();
    }

    public void setTitle(String title) {
        this.trackMeta = new TrackMeta(title, getArtist(), getPath(), getDuration(), getColor());
    }

    public void setArtist(String artist) {
        this.trackMeta = new TrackMeta(getTitle(), artist, getPath(), getDuration(), getColor());
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
        return trackMeta.getArtist();
    }

    public String getTitle() {
        return trackMeta.getTitle();
    }

    public String getPath() {
        return trackMeta.getPath();
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
        return trackMeta.getDuration();
    }

    public int getColor() {
        return trackMeta.getColor();
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
