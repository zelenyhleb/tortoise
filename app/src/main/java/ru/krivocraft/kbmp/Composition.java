package ru.krivocraft.kbmp;

import android.support.annotation.NonNull;

import java.io.Serializable;

class Composition implements Serializable {

    private String duration;

    private String composer;
    private String name;
    private String path;

    private int identifier;

    Composition(@NonNull String duration, String composer, String name, @NonNull String path, int identifier) {

        this.duration = duration;
        this.composer = composer;
        this.name = name;
        this.path = path;
        this.identifier = identifier;

        if (composer == null) {
            this.composer = "Unknown Artist";
        }
        if (name == null) {
            this.name = "Unknown Composition";
        }

    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this){
            return true;
        }
        if (!(obj instanceof Composition)){
            return false;
        }
        Composition composition = (Composition) obj;
        return composition.identifier == identifier;
    }

    @Override
    public int hashCode() {
        return identifier*17;
    }

    String getComposer() {
        return composer;
    }

    String getName() {
        return name;
    }

    String getPath() {
        return path;
    }

    String getDuration() {
        return duration;
    }
}
