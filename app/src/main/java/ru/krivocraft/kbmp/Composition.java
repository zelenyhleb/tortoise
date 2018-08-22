package ru.krivocraft.kbmp;

public class Composition {

    private long duration;

    private String composer;
    private String name;
    private String path;

    public Composition(long duration, String composer, String name, String path){

        this.duration = duration;
        this.composer = composer;
        this.name = name;
        this.path = path;

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

    public long getDuration() {
        return duration;
    }
}
