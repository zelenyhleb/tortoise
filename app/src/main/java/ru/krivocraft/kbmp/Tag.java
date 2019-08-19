package ru.krivocraft.kbmp;

import com.google.gson.Gson;

import java.util.Objects;

class Tag {
    String text;

    Tag(String text) {
        this.text = text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tag tag = (Tag) o;
        return Objects.equals(text, tag.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text);
    }

    String toJson() {
        return new Gson().toJson(this);
    }

    static Tag fromJson(String json) {
        return new Gson().fromJson(json, Tag.class);
    }

}
