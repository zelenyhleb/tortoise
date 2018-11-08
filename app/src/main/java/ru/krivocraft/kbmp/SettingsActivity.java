package ru.krivocraft.kbmp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ListView databaseView = findViewById(R.id.listdb);

        List<Track> tracks = new SQLiteProcessor(this).readCompositions(null, null);
        List<String> trackDatas = new ArrayList<>();
        for (Track track : tracks) {
            trackDatas.add(track.getIdentifier() + " | " + track.getName() + " | " + track.getArtist() + " | " + track.getPath());
        }

        databaseView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, trackDatas));
    }

    public void onClick(View v) {
        new SQLiteProcessor(this).clearDatabase();
        supportFinishAfterTransition();
    }
}
