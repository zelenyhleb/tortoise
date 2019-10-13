package ru.krivocraft.kbmp.utils;

import org.junit.Test;
import ru.krivocraft.kbmp.core.utils.Milliseconds;

import static org.junit.Assert.assertEquals;

public class MillisecondsTest {

    @Test
    public void lossless() {
        assertEquals(8, new Milliseconds(8000).seconds());
    }

    @Test
    public void lossy() {
        assertEquals(9, new Milliseconds(8200).seconds());
    }

    @Test
    public void nil() {
        assertEquals(0, new Milliseconds(0).seconds());
    }

    @Test
    public void tail() {
        assertEquals(1, new Milliseconds(239).seconds());
    }

    @Test
    public void negative() {
        assertEquals(-1, new Milliseconds(-1234).seconds());
    }
}
