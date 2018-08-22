package ru.krivocraft.kbmp;

import android.support.annotation.NonNull;

public class Composition {

    private String duration;

    private String composer;
    private String name;
    private String path;

    public Composition(@NonNull String duration, String composer, String name, @NonNull String path) {

        this.duration = duration;
        this.composer = composer;
        this.name = name;
        this.path = path;

        if (composer == null) {
            composer = "unknown";
        }
        if (name == null) {
            name = "unknown";
        }

    }


    public String getComposer() {
        return composer;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public String getDuration() {
        return duration;
    }
}
