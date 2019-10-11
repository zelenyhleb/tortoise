package ru.krivocraft.kbmp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
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

import ru.krivocraft.kbmp.api.TracksStorageManager;
import ru.krivocraft.kbmp.tasks.GetAlbumArtTask;

class TrackListAdapter extends ArrayAdapter<TrackList> {

    private ThumbnailStorageManager thumbnailStorageManager;
    private TracksStorageManager tracksStorageManager;

    TrackListAdapter(List<TrackList> trackLists, @NonNull Context context) {
        super(context, R.layout.playlists_grid_item, trackLists);
        this.tracksStorageManager = new TracksStorageManager(context);
        this.thumbnailStorageManager = new ThumbnailStorageManager();
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TrackList trackList = getItem(position);

        View itemView;
        if (convertView != null) {
            itemView = convertView;
        } else {
            itemView = LayoutInflater.from(getContext()).inflate(R.layout.playlists_grid_item, null);
        }

        ImageView imageView = itemView.findViewById(R.id.fragment_playlist_picture);

        if (trackList != null) {
            Bitmap art = thumbnailStorageManager.readThumbnail(trackList.getIdentifier());
            if (art != null) {
                imageView.setImageBitmap(art);
            } else {
                GetAlbumArtTask task = new GetAlbumArtTask(bitmap -> {
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                        try {
                            thumbnailStorageManager.writeThumbnail(trackList.getIdentifier(), bitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        imageView.setImageDrawable(getContext().getDrawable(R.drawable.ic_track_image_default));
                    }
                });
                task.execute(tracksStorageManager.getTracks(trackList.getTrackReferences()).toArray(new Track[0]));
            }

            ((TextView) itemView.findViewById(R.id.fragment_playlist_name)).setText(trackList.getDisplayName());
        }

        imageView.setClipToOutline(true);
        return itemView;
    }
}
