package ru.krivocraft.kbmp;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UtilsTest {

    @Test
    public void checkTime() {
        Track track = Utils.loadData("/storage/BB8A-1916/Music/1550266253_loqiemean-petlya-2019-muzonov_net.mp3");
        assertEquals(track.getTitle(), "Петля");
    }
}
