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

package ru.krivocraft.tortoise.android.editors;

import android.content.ContentValues;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import ru.krivocraft.tortoise.R;
import ru.krivocraft.tortoise.core.model.Track;
import ru.krivocraft.tortoise.core.model.TrackReference;
import ru.krivocraft.tortoise.android.tracklist.TracksStorageManager;
import ru.krivocraft.tortoise.android.ui.TextChangeSolver;

public class TrackEditorActivity extends AppCompatActivity {

    private Track source;
    private Track changed;
    private TrackReference reference;
    public static final String EXTRA_TRACK = "track";
    private TracksStorageManager tracksStorageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_editor);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Edit track");
        }

        reference = TrackReference.fromJson(getIntent().getStringExtra(EXTRA_TRACK));

        this.tracksStorageManager = new TracksStorageManager(this);

        changed = tracksStorageManager.getTrack(reference);
        source = tracksStorageManager.getTrack(reference);

        EditText title = findViewById(R.id.metadata_editor_title_edit);
        EditText artist = findViewById(R.id.metadata_editor_artist_edit);
        Switch ignored = findViewById(R.id.metadata_editor_ignored_edit);

        title.setText(changed.getTitle());
        artist.setText(changed.getArtist());
        ignored.setChecked(changed.isIgnored());

        Button cancel = findViewById(R.id.metadata_editor_button_cancel);
        cancel.setOnClickListener(v -> showNotSavedPrompt());

        Button apply = findViewById(R.id.metadata_editor_button_apply);
        apply.setOnClickListener(v -> {
            String selection = MediaStore.Audio.Media.DATA + " = ?";
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.Audio.Media.ARTIST, artist.getText().toString());
            contentValues.put(MediaStore.Audio.Media.TITLE, title.getText().toString());
            String[] args = {
                    changed.getPath()
            };
            getContentResolver().update(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues, selection, args);

            tracksStorageManager.updateTrack(changed);

            finish();
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
    }

    public void onBackPressed() {
        showNotSavedPrompt();
    }

    public void setReference(TrackReference reference) {
        this.reference = reference;
    }

    private void showNotSavedPrompt() {
        if (!source.equals(changed)) {
            AlertDialog ad = new AlertDialog.Builder(this)
                    .setMessage("Do you really want to leave without saving?")
                    .setTitle("Wait!")
                    .setPositiveButton("Yes", (dialog, which) -> finish())
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .create();
            ad.show();
        } else {
            finish();
        }
    }

}
