package ru.krivocraft.kbmp;

import com.google.gson.Gson;

class Tag {
    String text;

    Tag(String text) {
        this.text = text;
    }

    String toJson() {
        return new Gson().toJson(this);
    }

    static Tag fromJson(String json) {
        return new Gson().fromJson(json, Tag.class);
    }

}
