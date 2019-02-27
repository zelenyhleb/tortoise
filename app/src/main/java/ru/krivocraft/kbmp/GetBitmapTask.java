package ru.krivocraft.kbmp;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import java.io.File;

class GetBitmapTask extends AsyncTask<File, Void, Bitmap> {

    private OnPictureProcessedListener listener;

    @Override
    protected Bitmap doInBackground(File... files) {
        return Utils.getTrackBitmap(files[0]);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (listener != null) {
            listener.onPictureProcessed(bitmap);
        }
    }

    void setListener(OnPictureProcessedListener listener) {
        this.listener = listener;
    }
}
