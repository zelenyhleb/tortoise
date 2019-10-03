package ru.krivocraft.kbmp;

import android.content.ContentValues;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import ru.krivocraft.kbmp.api.TracksStorageManager;
import ru.krivocraft.kbmp.constants.Constants;

public class TrackEditorActivity extends AppCompatActivity {

    Track source;
    Track changed;

    private TracksStorageManager tracksStorageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metadata_editor);

        this.tracksStorageManager = new TracksStorageManager(this);

        TrackReference trackReference = TrackReference.fromJson(getIntent().getStringExtra(Constants.Extras.EXTRA_TRACK));
        changed = tracksStorageManager.getTrack(trackReference);
        source = tracksStorageManager.getTrack(trackReference);

        EditText title = findViewById(R.id.metadata_editor_title_edit);
        EditText artist = findViewById(R.id.metadata_editor_artist_edit);

        title.setText(changed.getTitle());
        artist.setText(changed.getArtist());

        Button cancel = findViewById(R.id.metadata_editor_button_cancel);
        cancel.setOnClickListener(v -> {
            showNotSavedPrompt();
        });

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

    @Override
    public void onBackPressed() {
        showNotSavedPrompt();
    }
}
