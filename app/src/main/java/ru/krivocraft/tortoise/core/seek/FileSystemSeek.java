package ru.krivocraft.tortoise.core.seek;

import ru.krivocraft.tortoise.core.model.Track;
import ru.krivocraft.tortoise.sorting.OnStorageUpdateCallback;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class FileSystemSeek extends SeekTask<File> {

    public FileSystemSeek(OnStorageUpdateCallback callback, boolean recognize, File seekBase) {
        super(callback, recognize, seekBase);
    }

    @Override
    public List<Track> seek(File directory) {
        List<Track> tracks = new LinkedList<>();
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    tracks.addAll(seek(file));
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
}
