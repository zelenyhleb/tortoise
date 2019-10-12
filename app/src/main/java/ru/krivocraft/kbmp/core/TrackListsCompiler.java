package ru.krivocraft.kbmp.core;

import android.content.Context;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ru.krivocraft.kbmp.core.storage.TracksStorageManager;
import ru.krivocraft.kbmp.core.track.Track;
import ru.krivocraft.kbmp.core.track.TrackList;
import ru.krivocraft.kbmp.tasks.compilers.CompileByAuthorTask;
import ru.krivocraft.kbmp.tasks.compilers.CompileFavoritesTask;
import ru.krivocraft.kbmp.tasks.compilers.CompileTrackListsTask;

public class TrackListsCompiler {
    private final TracksStorageManager tracksStorageManager;

    public TrackListsCompiler(Context context) {
        this.tracksStorageManager = new TracksStorageManager(context);
    }

    public void compileByAuthors(OnTrackListsCompiledCallback callback) {
        CompileByAuthorTask task = new CompileByAuthorTask();
        compile(task, TrackList.TRACK_LIST_BY_AUTHOR, callback);
    }

    public void compileFavorites(OnTrackListsCompiledCallback callback) {
        CompileFavoritesTask task = new CompileFavoritesTask();
        compile(task, TrackList.TRACK_LIST_CUSTOM, callback);
    }

    private void compile(CompileTrackListsTask task, int trackListType, OnTrackListsCompiledCallback callback) {
        List<TrackList> list = new LinkedList<>();
        task.setListener(trackLists -> new Thread(() -> parseMap(callback, list, trackLists, trackListType)).start());
        task.execute(tracksStorageManager.getTrackStorage().toArray(new Track[0]));
    }

    private void parseMap(OnTrackListsCompiledCallback callback, List<TrackList> list, Map<String, List<Track>> trackLists, int trackListByTag) {
        for (Map.Entry<String, List<Track>> entry : trackLists.entrySet()) {
            TrackList trackList = new TrackList(entry.getKey(), tracksStorageManager.getReferences(entry.getValue()), trackListByTag);
            list.add(trackList);
        }
        callback.onTrackListsCompiled(list);
    }

}
