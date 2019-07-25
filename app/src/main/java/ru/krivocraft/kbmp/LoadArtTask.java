package ru.krivocraft.kbmp;

import android.graphics.Bitmap;
import android.os.AsyncTask;

public class LoadArtTask extends AsyncTask<String, Void, Bitmap> {

    private BitmapDecoderCallback callback;

    @Override
    protected Bitmap doInBackground(String... strings) {
        return Utils.loadArt(strings[0]);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        callback.onBitmapDecoded(bitmap);
    }

    void setCallback(BitmapDecoderCallback callback) {
        this.callback = callback;
    }

    interface BitmapDecoderCallback{
        void onBitmapDecoded(Bitmap bitmap);
    }
}
