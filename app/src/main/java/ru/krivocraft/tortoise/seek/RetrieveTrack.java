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

package ru.krivocraft.tortoise.seek;

import android.media.MediaMetadataRetriever;
import ru.krivocraft.tortoise.core.model.Track;
import ru.krivocraft.tortoise.core.model.track.TrackMeta;
import ru.krivocraft.tortoise.thumbnail.Colors;

import java.io.File;

public class RetrieveTrack {
    private final MediaMetadataRetriever retriever;
    private final boolean recognize;

    private static final String UNKNOWN_ARTIST = "Unknown Artist";
    private static final String UNKNOWN_COMPOSITION = "Unknown Track";

    public RetrieveTrack(boolean recognize) {
        this.recognize = recognize;
        this.retriever = new MediaMetadataRetriever();
    }

    public Track from(File file) {
        retriever.setDataSource(file.getAbsolutePath());
        int duration = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        String title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        String artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        String path = file.getAbsolutePath();
        int color = Colors.getRandomColor();

        if (title == null) {
            String fileName = file.getName().substring(0, file.getName().length() - 4);
            if (recognize) {
                String[] meta = fileName.split(" - ");
                if (meta.length > 1) {
                    artist = meta[0];
                    title = meta[1];
                } else {
                    meta = fileName.split(" — ");
                    if (meta.length > 1) {
                        artist = meta[0];
                        title = meta[1];
                    } else {
                        title = fileName;
                        artist = UNKNOWN_ARTIST;
                    }
                }
            } else {
                title = fileName;
                artist = UNKNOWN_ARTIST;
            }
        }
        return new Track(new TrackMeta(title, artist, path, duration, color), 0);
    }
}
