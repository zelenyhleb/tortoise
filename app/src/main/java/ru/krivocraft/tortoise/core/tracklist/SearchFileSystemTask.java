package ru.krivocraft.tortoise.core.tracklist;

import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Environment;
import ru.krivocraft.tortoise.core.model.Track;
import ru.krivocraft.tortoise.sorting.OnStorageUpdateCallback;
import ru.krivocraft.tortoise.thumbnail.Colors;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class SearchFileSystemTask extends AsyncTask<Void, Integer, List<Track>> {

    private final OnStorageUpdateCallback callback;
    private final boolean recognize;

    public SearchFileSystemTask(OnStorageUpdateCallback callback, boolean recognize) {
        this.callback = callback;
        this.recognize = recognize;
    }

    @Override
    protected List<Track> doInBackground(Void... voids) {
        return search(Environment.getExternalStorageDirectory());
    }

    @Override
    protected void onPostExecute(List<Track> tracks) {
        super.onPostExecute(tracks);
        callback.onStorageUpdate(tracks);
    }

    private List<Track> search(File directory) {
        List<Track> tracks = new LinkedList<>();
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    tracks.addAll(search(file));
                } else {
                    if (file.getPath().endsWith(".mp3")) {
                        tracks.add(fromFile(file));
                    }
                }
            }
        } else {
            System.out.println("search failed");
        }
        return tracks;
    }

    private Track fromFile(File file) {
        System.out.println(file.getPath());
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(file.getAbsolutePath());
        long duration = Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        System.out.println(duration);
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

        return new Track(duration, artist, title, path, color);
    }
}
