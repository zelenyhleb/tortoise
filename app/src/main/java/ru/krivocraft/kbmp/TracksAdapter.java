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

class TracksAdapter extends ArrayAdapter<String> {

    private Context context;

    TracksAdapter(List<String> trackList, Context context) {
        super(context, R.layout.track_list_item, trackList);
        this.context = context;
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String path = getItem(position);
        Track track = Utils.loadData(path, context.getContentResolver());

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.track_list_item, null);
        }
        if (path != null) {

            ImageView trackImage = convertView.findViewById(R.id.item_track_image);
            ImageView trackState = convertView.findViewById(R.id.item_track_state);

            if (!track.isSelected()) {
                trackImage.setAlpha(1.0f);
//                Bitmap art = Utils.loadArt(path);
//                if (art != null) {
//                    trackImage.setImageBitmap(art);
//                } else {
                    trackImage.setImageDrawable(context.getDrawable(R.drawable.ic_track_image_default));
//                }
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
