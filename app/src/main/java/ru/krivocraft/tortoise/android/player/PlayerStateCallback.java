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

package ru.krivocraft.tortoise.android.player;

import android.support.v4.media.session.PlaybackStateCompat;
import androidx.core.util.Consumer;
import ru.krivocraft.tortoise.core.model.Track;

public final class PlayerStateCallback {

    private final Consumer<PlaybackStateCompat> stateChanged;
    private final Consumer<Track> trackChanged;

    public PlayerStateCallback(Consumer<PlaybackStateCompat> stateChanged, Consumer<Track> trackChanged) {
        this.stateChanged = stateChanged;
        this.trackChanged = trackChanged;
    }

    public void onPlaybackStateChanged(PlaybackStateCompat stateCompat) {
        stateChanged.accept(stateCompat);
    }

    public void onTrackChanged(Track track) {
        trackChanged.accept(track);
    }

}
