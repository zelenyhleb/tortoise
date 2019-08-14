package ru.krivocraft.kbmp;

import android.graphics.Color;

import com.google.gson.Gson;

class Tag {
    String text;
    Color color;

    Tag(String text, Color color) {

    }

    String toJson() {
        return new Gson().toJson(this);
    }

    static Tag fromJson(String json) {
        return new Gson().fromJson(json, Tag.class);
    }

}
