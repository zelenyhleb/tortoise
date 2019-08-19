package ru.krivocraft.kbmp;

import android.os.AsyncTask;

import java.util.List;
import java.util.Map;

public abstract class CompileTrackListsTask extends AsyncTask<Track, Integer, Map<String, List<Track>>> {

    private OnTrackListsCompiledListener listener;

    void setListener(OnTrackListsCompiledListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onPostExecute(Map<String, List<Track>> stringListMap) {
        super.onPostExecute(stringListMap);
        listener.onTrackListsCompiled(stringListMap);
    }
}
