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

import java.util.List;

import ru.krivocraft.kbmp.tasks.GetAlbumArtTask;
import ru.krivocraft.kbmp.tasks.OnAlbumArtAcquiredCallback;

class TrackListAdapter extends ArrayAdapter<TrackList> {

    private Context context;

    TrackListAdapter(List<TrackList> trackLists, @NonNull Context context) {
        super(context, R.layout.playlists_grid_item, trackLists);
        this.context = context;
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
            ((TextView) convertView.findViewById(R.id.fragment_playlist_name)).setText(trackList.getDisplayName());
            GetAlbumArtTask task = new GetAlbumArtTask(new OnAlbumArtAcquiredCallback() {
                @Override
                public void onAlbumArtAcquired(Bitmap bitmap) {
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                    }
                }
            });
            task.execute(Tracks.getTracks(context, trackList.getTrackReferences()).toArray(new Track[0]));
        }

        imageView.setClipToOutline(true);
        return convertView;
    }
}
