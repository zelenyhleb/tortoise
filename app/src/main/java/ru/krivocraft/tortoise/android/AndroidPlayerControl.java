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

import android.support.v4.media.session.MediaControllerCompat;
import ru.krivocraft.tortoise.core.api.Control;

public class AndroidPlayerControl implements Control {

    private final MediaControllerCompat.TransportControls controls;

    public AndroidPlayerControl(MediaControllerCompat.TransportControls controls) {
        this.controls = controls;
    }

    @Override
    public void play() {
        controls.play();
    }

    @Override
    public void pause() {
        controls.pause();
    }

    @Override
    public void next() {
        controls.skipToNext();
    }

    @Override
    public void previous() {
        controls.skipToPrevious();
    }

    @Override
    public void seek(int position) {
        controls.seekTo(position);
    }
}
