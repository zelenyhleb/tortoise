package ru.krivocraft.kbmp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.concurrent.ExecutionException;

class TracksAdapter extends ArrayAdapter<Track> {

    private Context context;

    TracksAdapter(List<Track> trackList, Context context) {
        super(context, R.layout.track_list_item, trackList);
        this.context = context;
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Track track = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.track_list_item, null);
        }
        if ((track != null ? track.getPath() : null) != null) {

            final ImageView trackImage = convertView.findViewById(R.id.item_track_image);
            final ImageView trackState = convertView.findViewById(R.id.item_track_state);

            if (!track.isSelected()) {

                LoadArtTask loadArtTask = new LoadArtTask();
                loadArtTask.execute(track.getPath());
                loadArtTask.setCallback(new LoadArtTask.BitmapDecoderCallback() {
                    @Override
                    public void onBitmapDecoded(Bitmap art) {
                        if (art != null) {
                            trackImage.setImageBitmap(art);
                        } else {
                            trackImage.setImageDrawable(context.getDrawable(R.drawable.ic_track_image_default));
                        }
                    }
                });

                trackImage.setImageDrawable(context.getDrawable(R.drawable.ic_track_image_default));

                trackImage.setAlpha(1.0f);
                trackImage.setClipToOutline(true);
                trackState.setImageDrawable(null);
            } else {
                trackImage.setAlpha(0.2f);
                if (track.isPlaying()) {
                    trackState.setImageDrawable(context.getDrawable(R.drawable.ic_pause));
                } else {
                    trackState.setImageDrawable(context.getDrawable(R.drawable.ic_play));
                }
            }

            ((TextView) convertView.findViewById(R.id.composition_name_text)).setText(track.getTitle());
            ((TextView) convertView.findViewById(R.id.composition_author_text)).setText(track.getArtist());
        }

        return convertView;
    }
}
