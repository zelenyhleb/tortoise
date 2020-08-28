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

package ru.krivocraft.tortoise.core.base;

import ru.krivocraft.tortoise.core.api.MediaPlayer;
import ru.krivocraft.tortoise.core.api.Playback;

public class BasePlayback implements Playback {

    private final Metadata metadata;
    private final MediaPlayer player;

    private boolean playing;

    public BasePlayback(Metadata metadata, MediaPlayer player) {
        this.metadata = metadata;
        this.player = player;
    }

    @Override
    public void start() {
        player.set(metadata.path());
        player.prepare();
    }

    @Override
    public void pause() {
        player.pause();
        playing = false;
    }

    @Override
    public void play() {
        player.play();
        playing = true;
    }

    @Override
    public void stop() {
        player.pause();
        player.reset();
        player.release();
    }

    @Override
    public void seekTo(long position) {
        player.seekTo((int) position);
    }

    @Override
    public State state() {
        return new BasePlaybackState(playing, true);
    }

    @Override
    public Metadata metadata() {
        return metadata;
    }

}
