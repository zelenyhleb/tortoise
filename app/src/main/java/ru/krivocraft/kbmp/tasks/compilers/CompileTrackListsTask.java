package ru.krivocraft.kbmp.tasks.compilers;

import android.os.AsyncTask;

import java.util.List;
import java.util.Map;

import ru.krivocraft.kbmp.core.track.Track;

public abstract class CompileTrackListsTask extends AsyncTask<Track, Integer, Map<String, List<Track>>> {

    private OnTrackListsCompileTaskCompleted listener;

    public void setListener(OnTrackListsCompileTaskCompleted listener) {
        this.listener = listener;
    }

    @Override
    protected void onPostExecute(Map<String, List<Track>> stringListMap) {
        super.onPostExecute(stringListMap);
        listener.onTrackListsCompiled(stringListMap);
    }

}
