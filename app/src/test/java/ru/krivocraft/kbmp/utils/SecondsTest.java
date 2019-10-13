package ru.krivocraft.kbmp.utils;

import org.junit.Test;
import ru.krivocraft.kbmp.core.utils.Seconds;

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
