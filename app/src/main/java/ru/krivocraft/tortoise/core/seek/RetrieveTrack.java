package ru.krivocraft.tortoise.core.seek;

import android.media.MediaMetadataRetriever;
import ru.krivocraft.tortoise.core.model.Track;
import ru.krivocraft.tortoise.core.model.track.TrackMeta;
import ru.krivocraft.tortoise.thumbnail.Colors;

import java.io.File;

public class RetrieveTrack {
    private final MediaMetadataRetriever retriever;
    private final boolean recognize;

    private static final String UNKNOWN_ARTIST = "Unknown Artist";
    private static final String UNKNOWN_COMPOSITION = "Unknown Track";

    public RetrieveTrack(boolean recognize) {
        this.recognize = recognize;
        this.retriever = new MediaMetadataRetriever();
    }

    public Track from(File file) {
        retriever.setDataSource(file.getAbsolutePath());
        long duration = Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        String title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        String artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        String path = file.getAbsolutePath();
        int color = Colors.getRandomColor();

        if (title == null) {
            String fileName = file.getName().substring(0, file.getName().length() - 4);
            if (recognize) {
                String[] meta = fileName.split(" - ");
                if (meta.length > 1) {
                    artist = meta[0];
                    title = meta[1];
                } else {
                    meta = fileName.split(" — ");
                    if (meta.length > 1) {
                        artist = meta[0];
                        title = meta[1];
                    }
                }
            } else {
                title = fileName;
                artist = "<unknown>";
            }
        }
        if ("<unknown>".equals(artist)) {
            artist = UNKNOWN_ARTIST;
        }
        if ("<unknown>".equals(title)) {
            title = UNKNOWN_COMPOSITION;
        }
        return new Track(new TrackMeta(title, artist, path, duration, color), 0);
    }
}
