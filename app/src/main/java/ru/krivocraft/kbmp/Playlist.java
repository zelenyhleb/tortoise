package ru.krivocraft.kbmp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class Playlist implements Serializable {

    private List<Composition> compositions = new ArrayList<>();

    void addComposition(Composition composition) {
        compositions.add(composition);
    }

    void removeComposition(Composition composition) {
        compositions.remove(composition);
    }

    void shuffle() {
    }

    Composition getComposition(int index) {
        return compositions.get(index);
    }

    void addCompositions(Collection<Composition> compositions) {
        this.compositions.addAll(compositions);
    }

    List<Composition> getCompositions() {
        return compositions;
    }

    int getSize() {
        return compositions.size();
    }

    int indexOf(Composition composition) {
        return compositions.indexOf(composition);
    }

    boolean contains(Composition composition) {
        return compositions.contains(composition);
    }

    boolean contains(String path) {
        for (Composition composition : compositions) {
            if (composition.getPath().equals(path))
                return true;
        }
        return false;
    }

}
