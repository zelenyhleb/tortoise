package ru.krivocraft.kbmp;

import android.content.Context;
import android.content.Intent;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.MotionEventCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;

import ru.krivocraft.kbmp.api.TracksStorageManager;
import ru.krivocraft.kbmp.constants.Constants;
import ru.krivocraft.kbmp.tasks.LoadArtTask;

public class TracksAdapter extends RecyclerView.Adapter<TracksAdapter.ViewHolder> implements ItemTouchHelperAdapter {
    private TrackList trackList;
    private Context context;
    private final boolean editingAllowed;
    private final boolean temp;
    private TracksStorageManager tracksStorageManager;
    private AdapterListener listener;

    TracksAdapter(TrackList trackList, Context context, boolean editingAllowed, boolean temp, AdapterListener listener) {
        this.trackList = trackList;
        this.context = context;
        this.editingAllowed = editingAllowed;
        this.temp = temp;
        this.tracksStorageManager = new TracksStorageManager(context);
        this.listener = listener;
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View root = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.track_list_item, viewGroup, false);
        return new ViewHolder(root, editingAllowed);
    }

    @Override
    public long getItemId(int position) {
        return Long.parseLong(trackList.get(position).toString());
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Track track = tracksStorageManager.getTrack(trackList.get(i));
        viewHolder.title.setText(track.getTitle());
        viewHolder.artist.setText(track.getArtist());
        viewHolder.reference = trackList.get(i);
        viewHolder.track = track;
        viewHolder.trackList = trackList;
        viewHolder.drawState(context);
        viewHolder.loadArt();
    }

    @Override
    public int getItemCount() {
        return trackList.size();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(trackList.getTrackReferences(), fromPosition, toPosition);

        if (listener != null) {
            listener.maintainRecyclerViewPosition(fromPosition, toPosition);
        }

        return true;
    }

    @Override
    public void onDragCompleted() {
        sendUpdate();
    }

    private void sendUpdate() {
        if (temp) {
            context.sendBroadcast(new Intent(Constants.Actions.ACTION_EDIT_PLAYING_TRACK_LIST).putExtra(Constants.Extras.EXTRA_TRACK_LIST, trackList.toJson()));
        } else {
            context.sendBroadcast(new Intent(Constants.Actions.ACTION_EDIT_TRACK_LIST).putExtra(Constants.Extras.EXTRA_TRACK_LIST, trackList.toJson()));
        }
    }

    interface AdapterListener {
        void maintainRecyclerViewPosition(int from, int to);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView artist;
        ImageView art;
        ImageView state;
        Track track;
        TrackReference reference;
        TrackList trackList;
        ImageButton more;

        ViewHolder(@NonNull View itemView, boolean editingAllowed) {
            super(itemView);
            title = itemView.findViewById(R.id.composition_name_text);
            artist = itemView.findViewById(R.id.composition_author_text);
            art = itemView.findViewById(R.id.item_track_image);
            state = itemView.findViewById(R.id.item_track_state);
            more = itemView.findViewById(R.id.button_more);
            itemView.setOnClickListener(new OnClickListener());
            if (editingAllowed) {
                more.setOnClickListener(v -> v.getContext().startActivity(new Intent(v.getContext(), TrackEditorActivity.class).putExtra(Constants.Extras.EXTRA_TRACK, reference.toJson())));
            } else {
                more.setVisibility(View.INVISIBLE);
            }
        }

        private void drawState(Context context) {
            if (track.isSelected()) {
                if (track.isPlaying()) {
                    state.setImageDrawable(context.getDrawable(R.drawable.ic_play));
                } else {
                    state.setImageDrawable(context.getDrawable(R.drawable.ic_pause));
                }
            } else {
                state.setImageDrawable(null);
            }
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
                serviceIntent.putExtra(Constants.Extras.EXTRA_TRACK, reference.toJson());
                serviceIntent.putExtra(Constants.Extras.EXTRA_TRACK_LIST, trackList.toJson());
                v.getContext().sendBroadcast(serviceIntent);

                Intent interfaceIntent = new Intent(Constants.Actions.ACTION_SHOW_PLAYER);
                v.getContext().sendBroadcast(interfaceIntent);
            }
        }
    }
}
