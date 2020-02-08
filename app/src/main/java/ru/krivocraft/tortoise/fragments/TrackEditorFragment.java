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

package ru.krivocraft.tortoise.fragments;

import android.content.ContentValues;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;

import android.widget.Switch;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import ru.krivocraft.tortoise.R;
import ru.krivocraft.tortoise.core.TextChangeSolver;
import ru.krivocraft.tortoise.core.storage.TracksStorageManager;
import ru.krivocraft.tortoise.core.track.Track;
import ru.krivocraft.tortoise.core.track.TrackReference;

public class TrackEditorFragment extends BaseFragment {

    @Override
    public void invalidate() {

    }

    private Track source;
    private Track changed;
    private OnTaskCompletedListener listener;
    private TrackReference reference;

    private TracksStorageManager tracksStorageManager;

    public static TrackEditorFragment newInstance(OnTaskCompletedListener listener, TrackReference reference) {
        TrackEditorFragment fragment = new TrackEditorFragment();
        fragment.setTitle("Edit Metadata");
        fragment.setListener(listener);
        fragment.setReference(reference);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_metadata_editor, container, false);

        this.tracksStorageManager = new TracksStorageManager(requireContext());

        changed = tracksStorageManager.getTrack(reference);
        source = tracksStorageManager.getTrack(reference);

        EditText title = rootView.findViewById(R.id.metadata_editor_title_edit);
        EditText artist = rootView.findViewById(R.id.metadata_editor_artist_edit);
        Switch ignored = rootView.findViewById(R.id.metadata_editor_ignored_edit);

        title.setText(changed.getTitle());
        artist.setText(changed.getArtist());
        ignored.setChecked(changed.isIgnored());

        Button cancel = rootView.findViewById(R.id.metadata_editor_button_cancel);
        cancel.setOnClickListener(v -> showNotSavedPrompt());

        Button apply = rootView.findViewById(R.id.metadata_editor_button_apply);
        apply.setOnClickListener(v -> {
            String selection = MediaStore.Audio.Media.DATA + " = ?";
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.Audio.Media.ARTIST, artist.getText().toString());
            contentValues.put(MediaStore.Audio.Media.TITLE, title.getText().toString());
            String[] args = {
                    changed.getPath()
            };
            requireContext().getContentResolver().update(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues, selection, args);

            tracksStorageManager.updateTrack(changed);

            listener.onComplete();
        });

        ignored.setOnCheckedChangeListener((compoundButton, b) -> changed.setIgnored(b));

        title.addTextChangedListener(new TextChangeSolver() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                changed.setTitle(s.toString());
            }
        });

        artist.addTextChangedListener(new TextChangeSolver() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                changed.setArtist(s.toString());
            }
        });
        return rootView;
    }

    public void onBackPressed() {
        showNotSavedPrompt();
    }

    public void setListener(OnTaskCompletedListener listener) {
        this.listener = listener;
    }

    public void setReference(TrackReference reference) {
        this.reference = reference;
    }

    private void showNotSavedPrompt() {
        if (!source.equals(changed)) {
            AlertDialog ad = new AlertDialog.Builder(requireContext())
                    .setMessage("Do you really want to leave without saving?")
                    .setTitle("Wait!")
                    .setPositiveButton("Yes", (dialog, which) -> listener.onComplete())
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .create();
            ad.show();
        } else {
            listener.onComplete();
        }
    }

    public interface OnTaskCompletedListener {
        void onComplete();
    }
}
