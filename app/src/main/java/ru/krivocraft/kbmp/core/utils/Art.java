package ru.krivocraft.kbmp.core.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;

public class Art {
    private final String path;

    public Art(String path) {
        this.path = path;
    }

    public Bitmap load() {
        byte[] embeddedPicture = pictureContent();
        if (embeddedPicture != null) {
            return BitmapFactory.decodeByteArray(embeddedPicture, 0, embeddedPicture.length);
        }
        return null;
    }

    private byte[] pictureContent() {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(path);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
        return retriever.getEmbeddedPicture();
    }
}
