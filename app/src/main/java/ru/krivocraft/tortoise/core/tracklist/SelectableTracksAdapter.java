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

package ru.krivocraft.tortoise.core.tracklist;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import ru.krivocraft.tortoise.R;
import ru.krivocraft.tortoise.core.model.Track;

import java.util.List;

public class SelectableTracksAdapter extends ArrayAdapter<Track> {

    public SelectableTracksAdapter(List<Track> trackList, Context context) {
        super(context, R.layout.track_list_item_selectable, trackList);
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        Track track = getItem(position);
        View itemView;

        if (convertView != null) {
            itemView = convertView;
        } else {
            itemView = LayoutInflater.from(getContext()).inflate(R.layout.track_list_item_selectable, null);
        }

        if (track != null) {
            ((TextView) itemView.findViewById(R.id.composition_name_text)).setText(track.getTitle());
            ((TextView) itemView.findViewById(R.id.composition_author_text)).setText(track.getArtist());
            ((CheckBox) itemView.findViewById(R.id.composition_checkbox)).setChecked(track.isCheckedInList());
        }

        return itemView;
    }


}
