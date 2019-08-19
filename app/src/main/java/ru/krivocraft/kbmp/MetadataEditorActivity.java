package ru.krivocraft.kbmp;

import android.content.ContentValues;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import ru.krivocraft.kbmp.constants.Constants;

public class MetadataEditorActivity extends AppCompatActivity {

    boolean addedTag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metadata_editor);
        TrackReference trackReference = TrackReference.fromJson(getIntent().getStringExtra(Constants.Extras.EXTRA_TRACK));
        Track track = Tracks.getTrack(this, trackReference);

        EditText title = findViewById(R.id.metadata_editor_title_edit);
        EditText artist = findViewById(R.id.metadata_editor_artist_edit);

        TextView tagsView = findViewById(R.id.tags_list);

        StringBuilder builder = new StringBuilder();
        List<Tag> tags = track.getTags();
        for (Tag tag : tags) {
            builder.append(tag.text);
            builder.append(", ");
        }
        if (builder.length() >= 3) {
            builder.replace(builder.length() - 2, builder.length(), ".");
        }
        tagsView.setText(builder);


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

            Tracks.updateTrack(this, trackReference, track);

            finish();
        });

        Button addTag = findViewById(R.id.button_add_tag);
        addTag.setOnClickListener(v -> {
            MetadataEditorActivity context = MetadataEditorActivity.this;

            View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_tag, null);
            AlertDialog ad = new AlertDialog.Builder(this)
                    .setMessage("Add Tag")
                    .setView(dialogView)
                    .setPositiveButton("ADD", null)
                    .create();

            ad.setOnShowListener(dialog -> {
                Button positiveButton = ad.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(v1 -> {
                    EditText editText = dialogView.findViewById(R.id.add_tag_edit_text);
                    Tag tag = new Tag(editText.getText().toString());
                    if (!track.getTags().contains(tag)) {
                        List<Tag> allTags = Tags.getAllTags(context);
                        if (!allTags.contains(tag)) {
                            Tags.createTag(context, tag);
                        }
                        track.addTag(tag);
                        tagsView.setText(builder.replace(builder.length() - 1, builder.length(), ", " + tag.text + "."));
                        apply.setEnabled(true);
                        addedTag = true;
                        dialog.dismiss();
                    } else {
                        Toast.makeText(context, "This track already has the same tag", Toast.LENGTH_LONG).show();
                    }
                });
            });

            ad.show();
        });

        title.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                apply.setEnabled(!s.equals(track.getTitle()) || addedTag);
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
