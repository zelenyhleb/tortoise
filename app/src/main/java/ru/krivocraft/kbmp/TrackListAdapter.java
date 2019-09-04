package ru.krivocraft.kbmp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.List;

import ru.krivocraft.kbmp.tasks.GetAlbumArtTask;
import ru.krivocraft.kbmp.tasks.OnAlbumArtAcquiredCallback;

class TrackListAdapter extends ArrayAdapter<TrackList> {

    private Context context;
    private ThumbnailStorageManager thumbnailStorageManager;

    TrackListAdapter(List<TrackList> trackLists, @NonNull Context context) {
        super(context, R.layout.playlists_grid_item, trackLists);
        this.context = context;
        this.thumbnailStorageManager = new ThumbnailStorageManager();
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TrackList trackList = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.playlists_grid_item, null);
        }
        ImageView imageView = convertView.findViewById(R.id.fragment_playlist_picture);

        if (trackList != null) {
            Bitmap art = thumbnailStorageManager.readThumbnail(trackList.getIdentifier());
            if (art != null) {
                imageView.setImageBitmap(art);
            } else {
                GetAlbumArtTask task = new GetAlbumArtTask(bitmap -> {
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                    }
                });
                task.execute(Tracks.getTracks(context, trackList.getTrackReferences()).toArray(new Track[0]));
            }

            ((TextView) convertView.findViewById(R.id.fragment_playlist_name)).setText(trackList.getDisplayName());
        }

        imageView.setClipToOutline(true);
        return convertView;
    }

    private int getSquareDimensions(Bitmap bitmap) {
        return Math.min(bitmap.getHeight(), bitmap.getWidth());
    }
}
