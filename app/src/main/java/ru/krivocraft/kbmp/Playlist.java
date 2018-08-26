package ru.krivocraft.kbmp;

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

    public Composition getComposition(int index) {
        return compositions.get(index);
    }

    public void addCompositions(Collection<Composition> compositions) {
        this.compositions.addAll(compositions);
    }

    public List<Composition> getCompositions() {
        return compositions;
    }

    public int getSize() {
        return compositions.size();
    }

    public int indexOf(Composition composition) {
        return compositions.indexOf(composition);
    }

}
