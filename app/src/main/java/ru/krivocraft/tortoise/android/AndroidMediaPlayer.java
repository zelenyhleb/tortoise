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

package ru.krivocraft.tortoise.android;


import ru.krivocraft.tortoise.core.api.MediaPlayer;

import java.io.IOException;

public class AndroidMediaPlayer implements MediaPlayer {

    private final android.media.MediaPlayer player;
    private final MediaPlayer.OnCompletedListener completed;
    private final MediaPlayer.OnPreparedListener prepared;

    public AndroidMediaPlayer(OnCompletedListener completed, OnPreparedListener prepared) {
        this.player = new android.media.MediaPlayer();
        this.completed = completed;
        this.prepared = prepared;
    }

    @Override
    public void set(String uri) {
        try {
            player.setDataSource(uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void play() {
        player.start();
    }

    @Override
    public void pause() {
        player.pause();
    }

    @Override
    public void seekTo(int position) {
        player.seekTo(position);
    }

    @Override
    public void reset() {
        player.reset();
    }

    @Override
    public void prepare() {
        player.setOnPreparedListener(this::onPrepared);
        player.setOnCompletionListener(this::onCompletion);
        player.prepareAsync();
    }

    @Override
    public void release() {
        player.release();
    }

    @Override
    public void stop() {
        player.stop();
    }

    @Override
    public int position() {
        return player.getCurrentPosition();
    }

    @Override
    public void volume(float volume) {
        player.setVolume(volume, volume);
    }

    @Override
    public boolean playing() {
        return player.isPlaying();
    }

    public void onCompletion(android.media.MediaPlayer mediaPlayer) {
        completed.onCompleted();
    }

    public void onPrepared(android.media.MediaPlayer mediaPlayer) {
        prepared.onPrepared();
    }
}
