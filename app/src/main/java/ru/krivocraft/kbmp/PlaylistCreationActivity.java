package ru.krivocraft.kbmp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
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

        ListView listView = findViewById(R.id.playlist_editor_list);
        listView.setAdapter(new Playlist(sqLiteProcessor.readCompositions(), this).getSelectableTracksAdapter());
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckBox checkBox = view.findViewById(R.id.composition_checkbox);
                if (selectedIds.contains(position)) {
                    checkBox.setChecked(false);
                    selectedIds.remove(Integer.valueOf(position));
                } else {
                    checkBox.setChecked(true);
                    selectedIds.add(position);
                }
            }
        });
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_apply:
                int i = new Random().nextInt();
                if (i < 0) {
                    i = i * -1;
                }
                commit(String.valueOf("playlist" + i));
                supportFinishAfterTransition();
                break;
        }
    }

    private void commit(String playlistName) {
        sqLiteProcessor.createPlaylist(playlistName);
        sqLiteProcessor.editPlaylist(playlistName, selectedIds);
    }
}
