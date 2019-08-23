package ru.krivocraft.kbmp;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import java.util.Objects;

class TrackReference {

    private int index;

    TrackReference(int index) {
        this.index = index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrackReference reference = (TrackReference) o;
        return index == reference.index;
    }

    @Override
    public int hashCode() {
        return Objects.hash(index);
    }

    @NonNull
    @Override
    public String toString() {
        return String.valueOf(index);
    }

    String toJson(){
        return new Gson().toJson(this);
    }

    static TrackReference fromJson(String in) {
        return new Gson().fromJson(in, TrackReference.class);
    }
}
