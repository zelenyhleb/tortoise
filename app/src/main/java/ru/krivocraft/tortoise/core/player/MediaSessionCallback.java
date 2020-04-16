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

package ru.krivocraft.tortoise.core.player;

import android.support.v4.media.session.MediaSessionCompat;

public class MediaSessionCallback extends MediaSessionCompat.Callback {

    private final PlaybackManager playbackManager;
    private final OnStopCallback onStopCallback;

    public MediaSessionCallback(PlaybackManager playbackManager, OnStopCallback onStopCallback) {
        this.playbackManager = playbackManager;
        this.onStopCallback = onStopCallback;
    }

    @Override
    public void onPlay() {
        playbackManager.play();
    }

    @Override
    public void onPause() {
        playbackManager.pause();
    }

    @Override
    public void onSkipToNext() {
        playbackManager.nextTrack();
    }

    @Override
    public void onSkipToPrevious() {
        playbackManager.previousTrack();
    }

    @Override
    public void onSeekTo(long pos) {
        playbackManager.seekTo((int) pos);
    }

    @Override
    public void onSkipToQueueItem(long id) {
        playbackManager.newTrack((int) id);
    }

    @Override
    public void onStop() {
        onStopCallback.onStop();
    }

    interface OnStopCallback {
        void onStop();
    }
}
