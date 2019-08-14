package ru.krivocraft.kbmp;

import android.content.ContentValues;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

import ru.krivocraft.kbmp.constants.Constants;

public class MetadataEditorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metadata_editor);
        Track track = Track.fromJson(getIntent().getStringExtra(Constants.Extras.EXTRA_TRACK));

        EditText title = findViewById(R.id.metadata_editor_title_edit);
        EditText artist = findViewById(R.id.metadata_editor_artist_edit);

        title.setText(track.getTitle());
        artist.setText(track.getArtist());

        Button cancel = findViewById(R.id.metadata_editor_button_cancel);
        cancel.setOnClickListener(v -> finish());

        Button apply = findViewById(R.id.metadata_editor_button_apply);
        apply.setEnabled(false);
        apply.setOnClickListener(v -> {
            String selection = MediaStore.Audio.Media.DATA + " = ?";
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.Audio.Media.ARTIST, artist.getText().toString());
            contentValues.put(MediaStore.Audio.Media.TITLE, title.getText().toString());
            String[] args = {
                    track.getPath()
            };
            getContentResolver().update(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues, selection, args);
            finish();
        });

        title.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                apply.setEnabled(!s.equals(track.getTitle()));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        artist.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                apply.setEnabled(!s.equals(track.getArtist()));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
}
