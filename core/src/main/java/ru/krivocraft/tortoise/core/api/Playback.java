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

package ru.krivocraft.tortoise.core.api;

import ru.krivocraft.tortoise.core.model.Track;

import java.util.List;

public interface Playback {

    void start();

    void pause();

    void play();

    void stop();

    void next();

    void previous();

    void skipTo(int index);

    void seekTo(int position);

    List<Track.Reference> tracks();

    Track.Reference current();

    interface State {

        boolean playing();

        boolean selected();
    }

    interface Metadata {

        String artist();

        String title();

        String path();

        int duration();

        /**
         * Each tortoise track has its own color
         * @return color code
         */
        int color();

    }

    interface Callback {

        void onStateChanged(State newState);

        void onTrackChanged(Metadata newMetadata);

    }
}
