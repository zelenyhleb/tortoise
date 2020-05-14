package ru.krivocraft.tortoise.core.seek;

import android.media.MediaMetadataRetriever;
import ru.krivocraft.tortoise.core.model.Track;
import ru.krivocraft.tortoise.thumbnail.Colors;

import java.io.File;

public class RetrieveTrack {
    private final MediaMetadataRetriever retriever;
    private final boolean recognize;

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
        return new Track(duration, artist, title, path, color, 0);
    }
}
