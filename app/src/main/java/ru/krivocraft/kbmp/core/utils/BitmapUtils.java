package ru.krivocraft.kbmp.core.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;

public class BitmapUtils {
    public static Bitmap loadArt(String path) {
        Bitmap art = null;

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(path);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }

        byte[] embeddedPicture = retriever.getEmbeddedPicture();

        if (embeddedPicture != null) {
            art = BitmapFactory.decodeByteArray(embeddedPicture, 0, embeddedPicture.length);
        }

        return art;
    }
}
