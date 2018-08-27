package ru.krivocraft.kbmp;

import android.os.AsyncTask;

import java.io.File;
import java.util.List;

class RecursiveSearchTask extends AsyncTask<String, Void, List<Composition>> {
    @Override
    protected List<Composition> doInBackground(String... strings) {
        return Utils.searchRecursively(new File(strings[0]));
    }
}
