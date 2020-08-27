/*
 * Copyright (c) 2020 Nikifor Fedorov
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
 *         Nikifor Fedorov and others
 */

package ru.krivocraft.tortoise.core.explorer;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import ru.krivocraft.tortoise.R;
import ru.krivocraft.tortoise.core.PreferencesManager;
import ru.krivocraft.tortoise.core.model.Track;
import ru.krivocraft.tortoise.core.model.TrackList;
import ru.krivocraft.tortoise.core.model.TrackReference;
import ru.krivocraft.tortoise.core.settings.SettingsStorageManager;
import ru.krivocraft.tortoise.core.tracklist.TracksStorageManager;
import ru.krivocraft.tortoise.core.sorting.GetAlbumArtTask;
import ru.krivocraft.tortoise.thumbnail.ThumbnailStorageManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TrackListsAdapter extends RecyclerView.Adapter<TrackListsAdapter.ViewHolder> {

    private final ThumbnailStorageManager thumbnailStorageManager;
    private final TracksStorageManager tracksStorageManager;
    private final OnClickListener listener;
    private final Context context;

    private List<TrackList> list;

    public TrackListsAdapter(List<TrackList> list, Context context, OnClickListener listener) {
        this.list = list;
        this.listener = listener;
        this.context = context;

        thumbnailStorageManager = new ThumbnailStorageManager();
        tracksStorageManager = new TracksStorageManager(context);
        setHasStableIds(true);

        if (!showIgnored()) {
            hideIgnored(list);
            notifyDataSetChanged();
        }
    }

    public void setList(List<TrackList> list) {
        this.list = list;
    }

    private boolean showIgnored() {
        return context.getSharedPreferences(PreferencesManager.STORAGE_SETTINGS, Context.MODE_PRIVATE).getBoolean(SettingsStorageManager.KEY_SHOW_IGNORED, false);
    }

    private void hideIgnored(List<TrackList> trackLists) {
        List<TrackList> hidden = new ArrayList<>();
        for (TrackList list : trackLists) {
            if (isHidden(list)) {
                hidden.add(list);
            }
        }
        trackLists.removeAll(hidden);
    }

    private boolean isHidden(TrackList trackList) {
        for (TrackReference reference : trackList.getTrackReferences()) {
            if (!tracksStorageManager.getTrack(reference).isIgnored()) {
                return false;
            }
        }
        return true;
    }

    @NonNull
    @Override
    public TrackListsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.playlists_grid_item, parent, false);
        return new ViewHolder(root, thumbnailStorageManager, tracksStorageManager, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackListsAdapter.ViewHolder holder, int position) {
        holder.trackList = list.get(position);
        holder.init();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public long getItemId(int position) {
        return list.get(position).hashCode();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView art;
        private final TextView title;
        private final ThumbnailStorageManager thumbnailStorageManager;
        private final TracksStorageManager tracksStorageManager;

        TrackList trackList;

        ViewHolder(@NonNull View itemView, ThumbnailStorageManager thumbnailStorageManager, TracksStorageManager tracksStorageManager, OnClickListener listener) {
            super(itemView);
            this.art = itemView.findViewById(R.id.fragment_playlist_picture);
            this.title = itemView.findViewById(R.id.fragment_playlist_name);
            this.thumbnailStorageManager = thumbnailStorageManager;
            this.tracksStorageManager = tracksStorageManager;
            itemView.setOnClickListener(view -> listener.onClick(trackList));
            itemView.setOnLongClickListener(view -> {
                if (trackList.getType() == TrackList.TRACK_LIST_CUSTOM
                        && !Objects.equals(trackList.getDisplayName(), TrackList.STORAGE_TRACKS_DISPLAY_NAME)
                        && !Objects.equals(trackList.getDisplayName(), TrackList.FAVORITES_DISPLAY_NAME)) {
                    listener.onLongClick(trackList);
                    return true;
                } else {
                    return false;
                }
            });
        }

        void init() {
            title.setText(trackList.getDisplayName());
            loadArt();
        }

        void loadArt() {
            Bitmap artBitmap = thumbnailStorageManager.readThumbnail(trackList.getIdentifier());
            if (artBitmap != null) {
                art.setImageBitmap(artBitmap);
            } else {
                GetAlbumArtTask task = new GetAlbumArtTask(bitmap -> {
                    if (bitmap != null) {
                        art.setImageBitmap(bitmap);
                        try {
                            thumbnailStorageManager.writeThumbnail(trackList.getIdentifier(), bitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        art.setImageDrawable(art.getContext().getDrawable(R.drawable.ic_track_image_default));
                    }
                });
                task.execute(tracksStorageManager.getTracks(trackList.getTrackReferences()).toArray(new Track[0]));
            }
            art.setClipToOutline(true);
        }
    }

    public interface OnClickListener {
        void onClick(TrackList trackList);

        void onLongClick(TrackList trackList);
    }
}
