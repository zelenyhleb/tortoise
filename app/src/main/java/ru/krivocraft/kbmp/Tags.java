package ru.krivocraft.kbmp;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import ru.krivocraft.kbmp.constants.Constants;

class Tags {
    static void createTag(Context context, Tag tag) {
        List<Tag> tags = getAllTags(context);
        tags.add(tag);
        writeAllTags(context, tags);
    }

    static void removeTag(Context context, Tag tag) {
        List<Tag> tags = getAllTags(context);
        tags.remove(tag);
        writeAllTags(context, tags);
    }

    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(Constants.STORAGE_TAGS, Context.MODE_PRIVATE);
    }

    private static void writeAllTags(Context context, List<Tag> allTags) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putString(Constants.KEY_TAGS, new Gson().toJson(allTags));
        editor.apply();
    }

    static List<Tag> getAllTags(Context context) {
        return new Gson().fromJson(getPreferences(context).getString(Constants.KEY_TAGS, new Gson().toJson(new ArrayList<>())), new TypeToken<List<Tag>>() {
        }.getType());
    }
}
