package ru.krivocraft.kbmp;

import android.content.ContentValues;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;

import co.lujun.androidtagview.TagContainerLayout;
import ru.krivocraft.kbmp.constants.Constants;

public class MetadataEditorActivity extends AppCompatActivity {

    Track source;
    Track changed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metadata_editor);
        TrackReference trackReference = TrackReference.fromJson(getIntent().getStringExtra(Constants.Extras.EXTRA_TRACK));
        changed = Tracks.getTrack(this, trackReference);
        source = Tracks.getTrack(this, trackReference);

        EditText title = findViewById(R.id.metadata_editor_title_edit);
        EditText artist = findViewById(R.id.metadata_editor_artist_edit);

        TagContainerLayout tagsView = findViewById(R.id.tags_list);

        tagsView.setTags(new ArrayList<>(CollectionUtils.collect(changed.getTags(), (tag) -> tag.text)));
        tagsView.setOnTagClickListener(new TagClickSolver() {
            @Override
            public void onTagLongClick(int position, String text) {
                tagsView.removeTag(position);
                changed.removeTag(changed.getTags().get(position));
            }
        });

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

            Tracks.updateTrack(this, trackReference, changed);

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
                    Tag tag = new Tag(editText.getText().toString().trim());
                    if (!changed.getTags().contains(tag)) {
                        changed.addTag(tag);
                        ArrayList<String> tags = new ArrayList<>(CollectionUtils.collect(changed.getTags(), input -> input.text));
                        System.out.println(tags);
                        tagsView.removeAllTags();
                        tagsView.setTags(tags);
                        apply.setEnabled(true);
                        dialog.dismiss();
                    } else {
                        Toast.makeText(context, "This track already has the same tag", Toast.LENGTH_LONG).show();
                    }
                });
            });

            ad.show();
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
