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

import android.support.v4.media.session.MediaSessionCompat;
import ru.krivocraft.tortoise.core.rating.Rating;

public class MediaSessionCallback extends MediaSessionCompat.Callback {

    private final PlaybackManager playback;
    private final OnStopCallback onStopCallback;
    private final Rating rating;

    public MediaSessionCallback(PlaybackManager playback, OnStopCallback onStopCallback, Rating rating) {
        this.playback = playback;
        this.onStopCallback = onStopCallback;
        this.rating = rating;
    }

    @Override
    public void onPlay() {
        playback.play();
    }

    @Override
    public void onPause() {
        playback.pause();
    }

    @Override
    public void onSkipToNext() {
        rating.rate(playback.selected(), -1);
        playback.next();
    }

    @Override
    public void onSkipToPrevious() {
        rating.rate(playback.selected(), -1);
        playback.previous();
    }

    @Override
    public void onSeekTo(long pos) {
        playback.seekTo((int) pos);
    }

    @Override
    public void onSkipToQueueItem(long id) {
        playback.skipTo((int) id);
    }

    @Override
    public void onStop() {
        onStopCallback.onStop();
    }

    interface OnStopCallback {
        void onStop();
    }
}
