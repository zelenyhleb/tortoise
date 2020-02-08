/*
 * Copyright (c) 2019 Nikifor Fedorov
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *     SPDX-License-Identifier: Apache-2.0
 *     Contributors:
 * 	    Nikifor Fedorov - whole development
 */

package ru.krivocraft.tortoise.core.track;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.devs.vectorchildfinder.VectorChildFinder;
import com.devs.vectorchildfinder.VectorDrawableCompat;
import com.google.android.material.snackbar.Snackbar;
import ru.krivocraft.tortoise.R;
import ru.krivocraft.tortoise.contexts.MainActivity;
import ru.krivocraft.tortoise.core.ColorManager;
import ru.krivocraft.tortoise.core.ItemTouchHelperAdapter;
import ru.krivocraft.tortoise.core.playback.MediaService;
import ru.krivocraft.tortoise.core.storage.TrackListsStorageManager;
import ru.krivocraft.tortoise.core.storage.TracksStorageManager;
import ru.krivocraft.tortoise.tasks.LoadArtTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TracksAdapter extends RecyclerView.Adapter<TracksAdapter.ViewHolder> implements ItemTouchHelperAdapter {
    private TrackList trackList;
    private Context context;
    private final boolean editingAllowed;
    private TracksStorageManager tracksStorageManager;
    private AdapterListener listener;
    private ColorManager colorManager;
    public TracksAdapter(TrackList trackList, Context context, boolean editingAllowed, boolean showIgnored, AdapterListener listener) {
        this.trackList = trackList;
        this.context = context;
        this.editingAllowed = editingAllowed;
        this.tracksStorageManager = new TracksStorageManager(context);
        this.colorManager = new ColorManager(context);
        this.listener = listener;
        setHasStableIds(true);
        if (!showIgnored) {
            hideIgnored();
        }
    }

    private void hideIgnored() {
        List<TrackReference> ignored = new ArrayList<>();
        for (TrackReference reference : this.trackList.getTrackReferences()) {
            if (tracksStorageManager.getTrack(reference).isIgnored()) {
                ignored.add(reference);
            }
        }
        this.trackList.removeAll(ignored);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View root = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.track_list_item, viewGroup, false);
        return new ViewHolder(root, editingAllowed, colorManager);
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
        viewHolder.loadArt(context);
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
    public void onClearView() {
        sendUpdate();
    }

    @Override
    public void onSwipe(RecyclerView.ViewHolder viewHolder) {
        int position = (int) viewHolder.getItemId();
        TrackReference item = new TrackReference(position);
        trackList.remove(item);

        if (editingAllowed && trackList.getDisplayName().equals(TrackListsStorageManager.STORAGE_TRACKS_DISPLAY_NAME)) {
            //If current playlist is "all tracks", you can hide track by swiping
            Track track = tracksStorageManager.getTrack(item);
            track.setIgnored(true);
            tracksStorageManager.updateTrack(track);
            Snackbar.make(viewHolder.itemView, "Track won't be shown in your collection anymore (You can get it back by enabling \"show hidden\" feature)", Snackbar.LENGTH_SHORT).show();
        } else {
            if (editingAllowed) {
                Snackbar.make(viewHolder.itemView, "Track removed from this playlist", Snackbar.LENGTH_SHORT).show();
            }
            sendUpdate();
        }
        notifyItemRemoved(position);
        notifyDataSetChanged();
    }

    private void sendUpdate() {
        if (!editingAllowed) {
            context.sendBroadcast(new Intent(MediaService.ACTION_EDIT_PLAYING_TRACK_LIST).putExtra(TrackList.EXTRA_TRACK_LIST, trackList.toJson()));
        } else {
            context.sendBroadcast(new Intent(MediaService.ACTION_EDIT_TRACK_LIST).putExtra(TrackList.EXTRA_TRACK_LIST, trackList.toJson()));
        }
    }

    public interface AdapterListener {
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
        ColorManager colorManager;

        ViewHolder(@NonNull View itemView, boolean editingAllowed, ColorManager colorManager) {
            super(itemView);
            this.colorManager = colorManager;
            title = itemView.findViewById(R.id.composition_name_text);
            artist = itemView.findViewById(R.id.composition_author_text);
            art = itemView.findViewById(R.id.item_track_image);
            state = itemView.findViewById(R.id.item_track_state);
            more = itemView.findViewById(R.id.button_more);
            itemView.setOnClickListener(new OnClickListener());
            if (editingAllowed) {
                more.setOnClickListener(v -> v.getContext().sendBroadcast(new Intent(MainActivity.ACTION_SHOW_TRACK_EDITOR).putExtra(Track.EXTRA_TRACK, reference.toJson())));
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

        void loadArt(Context context) {
            art.setClipToOutline(true);

            LoadArtTask loadArtTask = new LoadArtTask();
            loadArtTask.setCallback(art -> {
                if (art != null) {
                    this.art.setImageBitmap(art);
                } else {
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
                        VectorChildFinder finder = new VectorChildFinder(this.art.getContext(), R.drawable.ic_track_image_default, this.art);
                        VectorDrawableCompat.VFullPath background = finder.findPathByName("background");
                        background.setFillColor(colorManager.getColor(track.getColor()));
                    } else {
                        this.art.setImageDrawable(context.getDrawable(R.drawable.ic_track_image_default));
                    }
                }
            });
            loadArtTask.execute(track.getPath());
        }

        private class OnClickListener implements View.OnClickListener {
            @Override
            public void onClick(View v) {
                Intent serviceIntent = new Intent(MediaService.ACTION_PLAY_FROM_LIST);
                serviceIntent.putExtra(Track.EXTRA_TRACK, reference.toJson());
                serviceIntent.putExtra(TrackList.EXTRA_TRACK_LIST, trackList.toJson());
                v.getContext().sendBroadcast(serviceIntent);

                Intent interfaceIntent = new Intent(MainActivity.ACTION_SHOW_PLAYER);
                v.getContext().sendBroadcast(interfaceIntent);
            }
        }
    }
}
