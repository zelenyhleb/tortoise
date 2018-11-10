package ru.krivocraft.kbmp;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PlaylistCreationActivity extends AppCompatActivity {

    private List<Integer> selectedIds = new ArrayList<>();
    private SQLiteProcessor sqLiteProcessor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_creation);

        sqLiteProcessor = new SQLiteProcessor(this);

        final ListView listView = findViewById(R.id.playlist_editor_list);
        listView.setAdapter(new Playlist(sqLiteProcessor.readCompositions(null, null), this).getSelectableTracksAdapter());
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckBox checkBox = view.findViewById(R.id.composition_checkbox);
                Track track = (Track) parent.getItemAtPosition(position);
                if (selectedIds.contains(position)) {
                    track.setChecked(false);
                    selectedIds.remove(Integer.valueOf(position));
                } else {
                    track.setChecked(true);
                    selectedIds.add(position);
                }
                checkBox.setChecked(track.isChecked());
            }
        });
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_apply:
                showDialog();
                break;
        }
    }

    private void showDialog() {
        final EditText view = new EditText(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setView(view)
                .setIcon(R.drawable.ic_launcher)
                .setTitle("Enter playlist name")
                .setPositiveButton("Commit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        commit(view.getText().toString());
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .create();
        builder.show();
    }

    private void commit(String playlistName) {
        sqLiteProcessor.createPlaylist(playlistName);
        sqLiteProcessor.editPlaylist(playlistName, selectedIds);
        //finish activity, we don't need it anymore
        supportFinishAfterTransition();
    }
}
