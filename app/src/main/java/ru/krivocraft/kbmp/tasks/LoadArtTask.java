package ru.krivocraft.kbmp.tasks;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import ru.krivocraft.kbmp.Utils;

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

    public void setCallback(BitmapDecoderCallback callback) {
        this.callback = callback;
    }

    public interface BitmapDecoderCallback{
        void onBitmapDecoded(Bitmap bitmap);
    }
}
