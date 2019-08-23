package ru.krivocraft.kbmp;

import android.content.Context;
import androidx.annotation.NonNull;
import android.widget.ArrayAdapter;

import java.util.List;

public class PlaylistAdapter extends ArrayAdapter<String> {

    public PlaylistAdapter(@NonNull Context context, int resource, @NonNull List<String> objects) {
        super(context, resource, objects);
    }
}
