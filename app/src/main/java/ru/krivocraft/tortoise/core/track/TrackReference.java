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

package ru.krivocraft.tortoise.core.track;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import java.util.Objects;

public class TrackReference {

    private int value;

    public TrackReference(int value) {
        this.value = value;
    }

    public TrackReference(Track track) {
        this.value = track.getIdentifier();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrackReference reference = (TrackReference) o;
        return value == reference.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    public int getValue() {
        return value;
    }

    @NonNull
    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    public static TrackReference fromJson(String in) {
        return new Gson().fromJson(in, TrackReference.class);
    }
}
