package ru.krivocraft.tortoise.seek;

import android.os.AsyncTask;
import ru.krivocraft.tortoise.core.model.Track;
import ru.krivocraft.tortoise.core.sorting.OnStorageUpdateCallback;

import java.util.List;

public abstract class SeekTask<T> extends AsyncTask<Void, Integer, List<Track>> {

    protected final OnStorageUpdateCallback callback;
    protected final RetrieveTrack retrieveTrack;
    protected final T seekBase;

    public SeekTask(OnStorageUpdateCallback callback, boolean recognize, T seekBase) {
        this.callback = callback;
        this.seekBase = seekBase;
        this.retrieveTrack = new RetrieveTrack(recognize);
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

}
