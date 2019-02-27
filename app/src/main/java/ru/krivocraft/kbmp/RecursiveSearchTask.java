package ru.krivocraft.kbmp;

import android.os.AsyncTask;

class RecursiveSearchTask extends AsyncTask<SearchTaskBundle, Void, Void> {
    @Override
    protected Void doInBackground(SearchTaskBundle... searchTaskBundles) {
        SearchTaskBundle searchTaskBundle = searchTaskBundles[0];
        Utils.search(searchTaskBundle.context, searchTaskBundle.listener, searchTaskBundle.allTracksTrackList);
        return null;
    }
}
