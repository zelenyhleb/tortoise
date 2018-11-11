package ru.krivocraft.kbmp;

import android.support.v4.app.Fragment;

abstract class AbstractTrackViewFragment extends Fragment implements Track.OnTrackStateChangedListener{

    public AbstractTrackViewFragment() {

    }

    abstract void invalidate();

}
