package ru.krivocraft.kbmp;

import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TrackListUnitTest {

    private final String artist1 = "artist1";
    private final String artist2 = "artist2";
    private final String artist3 = "artist3";

    private final String title1 = "title1";
    private final String title2 = "title2";
    private final String title3 = "title3";

    private final String path1 = "path1";
    private final String path2 = "path2";
    private final String path3 = "path3";

    private final List<String> paths = Arrays.asList(path1, path2, path3);

    private TrackList newInstance() {
        ArrayList<Track> trackList = new ArrayList<>();

        Track track1 = Mockito.mock(Track.class);
        Mockito.when(track1.getPath()).thenReturn(path1);

        trackList.add(track1);
        trackList.add(Mockito.mock(Track.class, (Answer) invocation -> path2));
        trackList.add(Mockito.mock(Track.class, (Answer) invocation -> path3));

        return new TrackList("test list", trackList);
    }

    @Test
    public void testGetPathList() {
        TrackList trackList = newInstance();
        List<String> paths = trackList.getPaths();
        assertEquals(this.paths, paths);
    }
}
