package ru.krivocraft.kbmp.tasks;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import ru.krivocraft.kbmp.core.utils.Art;

public class LoadArtTask extends AsyncTask<String, Void, Bitmap> {

    private BitmapDecoderCallback callback;

    @Override
    protected Bitmap doInBackground(String... strings) {
        return new Art(strings[0]).load();
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        callback.onBitmapDecoded(bitmap);
    }

    public void setCallback(BitmapDecoderCallback callback) {
        this.callback = callback;
    }

    public interface BitmapDecoderCallback {
        void onBitmapDecoded(Bitmap bitmap);
    }
}
