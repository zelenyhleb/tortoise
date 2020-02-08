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

package ru.krivocraft.tortoise.contexts;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import ru.krivocraft.tortoise.R;
import ru.krivocraft.tortoise.core.Searcher;
import ru.krivocraft.tortoise.core.TextChangeSolver;
import ru.krivocraft.tortoise.core.storage.ThumbnailStorageManager;
import ru.krivocraft.tortoise.core.storage.TrackListsStorageManager;
import ru.krivocraft.tortoise.core.storage.TracksStorageManager;
import ru.krivocraft.tortoise.core.track.SelectableTracksAdapter;
import ru.krivocraft.tortoise.core.track.Track;
import ru.krivocraft.tortoise.core.track.TrackList;
import ru.krivocraft.tortoise.core.track.TrackReference;

import java.io.IOException;
import java.util.List;

public class TrackListEditorActivity extends BaseActivity {

    private static final int GALLERY_REQUEST_CODE = 2831;
    public static final String EXTRA_CREATION = "creation";

    private TrackList source;
    private TrackList changed;

    private ThumbnailStorageManager thumbnailStorageManager;
    private TracksStorageManager tracksStorageManager;

    private ImageView art;

    private Bitmap selectedBitmap;
    private boolean pictureChanged = false;

    private boolean creation = false;

    private final String TYPE_IMAGE = "image/*";
    private final String[] MIME_TYPES = new String[]{"image/jpeg", "image/png"};
    private TrackListsStorageManager trackListsStorageManager;
    private ListView listView;
    private SelectableTracksAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_list_editor);

        initTools();
        initList();
        initTitleEditor();
        initSearch();
        initApplyButton();
        initPickButton();
        initDeleteButton();
        initCancelButton();

        createActionBarTitle();
        setTrackListThumbnail();
    }

    private void initCancelButton() {
        Button cancel = findViewById(R.id.track_list_editor_button_cancel);
        cancel.setOnClickListener(v -> {
            if (!creation) {
                showNotSavedPrompt();
            } else {
                finish();
            }
        });
    }

    private void initDeleteButton() {
        Button delete = findViewById(R.id.track_list_editor_button_delete);
        if (!creation) {
            delete.setOnClickListener(v -> {
                AlertDialog dialog = new AlertDialog.Builder(TrackListEditorActivity.this)
                        .setTitle("Are you sure?")
                        .setMessage("Do you really want to delete " + source.getDisplayName() + "?")
                        .setPositiveButton("DELETE", (dialog12, which) -> {
                            trackListsStorageManager.removeTrackList(source);
                            thumbnailStorageManager.removeThumbnail(source.getIdentifier());
                            finish();
                        })
                        .setNegativeButton("CANCEL", (dialog1, which) -> dialog1.dismiss())
                        .create();
                dialog.show();
            });
        } else {
            delete.setVisibility(View.GONE);
        }
    }

    private void initPickButton() {
        Button pick = findViewById(R.id.track_list_editor_button_pick);
        pick.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType(TYPE_IMAGE);
            intent.putExtra(Intent.EXTRA_MIME_TYPES, MIME_TYPES);
            startActivityForResult(intent, GALLERY_REQUEST_CODE);
        });
    }

    private void initSearch() {
        EditText search = findViewById(R.id.track_list_editor_search);
        search.addTextChangedListener(new TextChangeSolver() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Searcher searcher = new Searcher(TrackListEditorActivity.this);
                List<Track> found = searcher.searchInTracks(s, tracksStorageManager.getTrackStorage());
                listView.setAdapter(new SelectableTracksAdapter(found, TrackListEditorActivity.this));
                if (s.length() < 1) {
                    listView.setAdapter(adapter);
                }
            }
        });
    }

    private void initTitleEditor() {
        EditText title = findViewById(R.id.track_list_editor_edit_text);
        title.setText(source.getDisplayName());
        title.addTextChangedListener(new TextChangeSolver() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                changed.setDisplayName(s.toString());
                changed.setIdentifier(TrackList.createIdentifier(s.toString()));
            }
        });
    }

    private void initApplyButton() {
        Button apply = findViewById(R.id.track_list_editor_button_apply);
        apply.setOnClickListener(v -> {
            if (checkTrackList(changed.size(), changed.getDisplayName(), TrackListEditorActivity.this)) {
                if (!creation) {
                    trackListsStorageManager.clearTrackList(changed.getIdentifier());
                    trackListsStorageManager.updateTrackListData(changed);
                    trackListsStorageManager.updateTrackListContent(changed);
                } else {
                    trackListsStorageManager.writeTrackList(new TrackList(changed.getDisplayName(), changed.getTrackReferences(), changed.getType()));
                }
            } else {
                return;
            }

            if (nameChanged() && !pictureChanged) {
                replaceThumbnail();
            }

            if (selectedBitmap != null) {
                try {
                    thumbnailStorageManager.writeThumbnail(changed.getIdentifier(), selectedBitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            finish();
        });
    }

    private void initList() {
        List<Track> trackStorage = tracksStorageManager.getTrackStorage();
        List<TrackReference> trackReferences = changed.getTrackReferences();
        flagExisting(trackStorage, trackReferences);

        adapter = new SelectableTracksAdapter(trackStorage, this);
        listView = findViewById(R.id.track_list_editor_list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Track item = (Track) parent.getItemAtPosition(position);
            TrackReference reference = new TrackReference(item);
            if (trackReferences.contains(reference)) {
                item.setCheckedInList(false);
                changed.remove(reference);
            } else {
                item.setCheckedInList(true);
                changed.add(reference);
            }
            adapter.notifyDataSetInvalidated();
        });
    }

    private void setTrackListThumbnail() {
        selectedBitmap = thumbnailStorageManager.readThumbnail(changed.getIdentifier());
        if (selectedBitmap != null) {
            this.art.setImageBitmap(selectedBitmap);
        } else {
            this.art.setImageDrawable(getDrawable(R.drawable.ic_icon));
        }
    }

    private void createActionBarTitle() {
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            if (creation) {
                supportActionBar.setTitle("Create Playlist");
            } else {
                supportActionBar.setTitle("Edit Playlist");
            }
        }
    }

    @Override
    void init() {
        //Do nothing
    }

    @Override
    void onPlaybackStateChanged(PlaybackStateCompat newPlaybackState) {
        //Do nothing
    }

    @Override
    void onMetadataChanged(MediaMetadataCompat newMetadata) {
        //Do nothing
    }

    @Override
    void onMediaBrowserConnected() {
        //Do nothing
    }

    private void initTools() {
        creation = getIntent().getBooleanExtra(EXTRA_CREATION, false);
        source = TrackList.fromJson(getIntent().getStringExtra(TrackList.EXTRA_TRACK_LIST));
        changed = TrackList.fromJson(getIntent().getStringExtra(TrackList.EXTRA_TRACK_LIST));
        art = findViewById(R.id.track_list_editor_image);
        art.setClipToOutline(true);

        thumbnailStorageManager = new ThumbnailStorageManager();
        tracksStorageManager = new TracksStorageManager(this);
        trackListsStorageManager = new TrackListsStorageManager(TrackListEditorActivity.this, TrackListsStorageManager.FILTER_ALL);
    }

    private void flagExisting(List<Track> trackStorage, List<TrackReference> trackReferences) {
        for (Track track : trackStorage) {
            if (trackReferences.contains(new TrackReference(track))) {
                track.setCheckedInList(true);
                tracksStorageManager.updateTrack(track);
            }
        }
    }

    private boolean checkTrackList(int arrayLength, String displayName, Context context) {
        if (displayName.length() <= 0) {
            Toast.makeText(context, "Name must not be empty", Toast.LENGTH_LONG).show();
            return false;
        }
        if (trackListsStorageManager.getExistingTrackListNames().contains(displayName) && !displayName.equals(source.getDisplayName())) {
            Toast.makeText(context, "The similar name already exists", Toast.LENGTH_LONG).show();
            return false;
        }
        if (displayName.length() > 20) {
            Toast.makeText(context, "Length must not exceed 20 characters", Toast.LENGTH_LONG).show();
            return false;
        }
        if (arrayLength <= 0) {
            Toast.makeText(context, "You can't create empty track list", Toast.LENGTH_LONG).show();
            return false;
        }
        if ("empty".equals(displayName)) {
            Toast.makeText(context, "Ha-ha, very funny. Name must not be empty", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private boolean nameChanged() {
        return !source.getIdentifier().equals(changed.getIdentifier());
    }

    private void replaceThumbnail() {
        try {
            thumbnailStorageManager.replaceThumbnail(source.getIdentifier(), changed.getIdentifier());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int getSquareDimensions(Bitmap bitmap) {
        return Math.min(bitmap.getHeight(), bitmap.getWidth());
    }

    private void showNotSavedPrompt() {
        if (!source.equals(changed) || pictureChanged) {
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

    @Override
    public void onBackPressed() {
        showNotSavedPrompt();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK
                && requestCode == GALLERY_REQUEST_CODE
                && data != null) {
            Uri selectedImage = data.getData();
            try {
                Bitmap input = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                Bitmap bitmap = ThumbnailUtils.extractThumbnail(input, getSquareDimensions(input), getSquareDimensions(input));
                art.setImageBitmap(bitmap);
                selectedBitmap = bitmap;
                pictureChanged = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
