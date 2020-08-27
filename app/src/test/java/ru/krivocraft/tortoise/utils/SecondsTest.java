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

package ru.krivocraft.tortoise.utils;

import org.junit.Test;
import ru.krivocraft.tortoise.core.utils.Seconds;

import static org.junit.Assert.assertEquals;

public class SecondsTest {

    @Test
    public void regular() {
        assertEquals("01:20", new Seconds(80).formatted());
    }

    @Test
    public void secondsOnly() {
        assertEquals("00:11", new Seconds(11).formatted());
    }

    @Test
    public void minutesOnly() {
        assertEquals("21:00", new Seconds(1260).formatted());
    }

    @Test
    public void nil() {
        assertEquals("00:00", new Seconds(0).formatted());
    }

    /**
     * Yep.
     * <p>
     * You see exactly what you see.
     * <p>
     * The test is added prior TestUtil refactoring in order to keep the functionality ad verbum.
     * <p>
     * TODO: assert throws IllegalArgumentException
     */
    @Test
    public void negative() {
        assertEquals("0-3:0-59", new Seconds(-239).formatted());
    }
}
