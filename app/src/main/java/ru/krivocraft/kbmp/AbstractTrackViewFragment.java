package ru.krivocraft.kbmp;

import android.support.v4.app.Fragment;

abstract class AbstractTrackViewFragment extends Fragment {

    public AbstractTrackViewFragment() {

    }

    abstract void invalidate();

}
