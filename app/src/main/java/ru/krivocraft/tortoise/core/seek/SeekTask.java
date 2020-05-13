package ru.krivocraft.tortoise.core.seek;

import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import ru.krivocraft.tortoise.core.model.Track;
import ru.krivocraft.tortoise.sorting.OnStorageUpdateCallback;
import ru.krivocraft.tortoise.thumbnail.Colors;

import java.io.File;
import java.util.List;

public abstract class SeekTask<T> extends AsyncTask<Void, Integer, List<Track>> {

    protected final OnStorageUpdateCallback callback;
    protected final boolean recognize;
    protected final T seekBase;

    public SeekTask(OnStorageUpdateCallback callback, boolean recognize, T seekBase) {
        this.callback = callback;
        this.recognize = recognize;
        this.seekBase = seekBase;
    }

    @Override
    protected List<Track> doInBackground(Void... voids) {
        return seek(seekBase);
    }

    @Override
    protected void onPostExecute(List<Track> tracks) {
        super.onPostExecute(tracks);
        callback.onStorageUpdate(tracks);
    }

    public abstract List<Track> seek(T t);

    protected Track fromFile(File file) {
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

        return new Track(duration, artist, title, path, color, 0);
    }
}
