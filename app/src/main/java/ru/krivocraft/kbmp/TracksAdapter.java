package ru.krivocraft.kbmp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import ru.krivocraft.kbmp.constants.Constants;

public class TracksAdapter extends RecyclerView.Adapter<TracksAdapter.ViewHolder> {
    private List<Track> tracks;
    private TrackList trackList;

    TracksAdapter(List<Track> tracks, TrackList trackList) {
        this.tracks = tracks;
        this.trackList = trackList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View root = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.track_list_item, viewGroup, false);
        return new ViewHolder(root);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.title.setText(tracks.get(i).getTitle());
        viewHolder.artist.setText(tracks.get(i).getArtist());
        viewHolder.track = tracks.get(i);
        viewHolder.trackList = trackList;
        viewHolder.loadArt();
    }

    @Override
    public int getItemCount() {
        return tracks.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView artist;
        ImageView art;
        Track track;
        TrackList trackList;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.composition_name_text);
            artist = itemView.findViewById(R.id.composition_author_text);
            art = itemView.findViewById(R.id.item_track_image);
            itemView.setOnClickListener(new OnClickListener());
        }

        void loadArt() {
            art.setClipToOutline(true);

            LoadArtTask loadArtTask = new LoadArtTask();
            loadArtTask.setCallback(art -> {
                if (art != null) {
                    this.art.setImageBitmap(art);
                } else {
                    this.art.setImageDrawable(this.art.getContext().getDrawable(R.drawable.ic_track_image_default));
                }
            });
            loadArtTask.execute(track.getPath());
        }

        private class OnClickListener implements View.OnClickListener {
            @Override
            public void onClick(View v) {
                Intent serviceIntent = new Intent(Constants.Actions.ACTION_PLAY_FROM_LIST);
                serviceIntent.putExtra(Constants.Extras.EXTRA_PATH, track.getPath());
                serviceIntent.putExtra(Constants.Extras.EXTRA_TRACK_LIST, trackList.toJson());
                v.getContext().sendBroadcast(serviceIntent);

                Intent interfaceIntent = new Intent(Constants.Actions.ACTION_SHOW_PLAYER);
                v.getContext().sendBroadcast(interfaceIntent);
            }
        }
    }
}
