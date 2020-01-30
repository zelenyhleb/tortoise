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

package ru.krivocraft.tortoise.core.utils;

public class Seconds {
    private final int seconds;

    public Seconds(int seconds) {
        //todo: consider throwing IllegalArgumentException for negative values
        this.seconds = seconds;
    }

    public String formatted() {
        int secondsTail = seconds % 60;
        int minutes = (seconds - secondsTail) / 60;
        return twoDigits(minutes) + ":" + twoDigits(secondsTail);
    }

    private String twoDigits(int numerical) {
        String formatted = String.valueOf(numerical);
        if (numerical < 10) {
            formatted = "0" + formatted;
        }
        return formatted;
    }
}
