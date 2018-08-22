package ru.krivocraft.kbmp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class Playlist implements Serializable {

    private List<Composition> compositions = new ArrayList<>();

    public void addComposition(Composition composition) {
        compositions.add(composition);
    }

    public void removeComposition(Composition composition) {
        compositions.remove(composition);
    }

    public void shuffle() {

    }

    public void addCompositions(Collection<Composition> compositions){
        this.compositions.addAll(compositions);
    }

    public List<Composition> getCompositions() {
        return compositions;
    }

}
