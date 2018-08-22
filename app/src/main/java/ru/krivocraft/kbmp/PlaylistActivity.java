package ru.krivocraft.kbmp;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class PlaylistActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
        Playlist playlist = new Playlist();
        playlist.addCompositions(Utils.search(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)));

        ListView playlistView = findViewById(R.id.playlist);
        playlistView.setAdapter(new PlaylistAdapter(playlist, this));
        playlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Composition composition = (Composition) adapterView.getItemAtPosition(i);
                Intent intent = new Intent(PlaylistActivity.this, PlayerActivity.class);
                intent.putExtra(Constants.COMPOSITION, composition);
                startActivity(intent);
            }
        });

        Intent serviceIntent = new Intent(this, PlayerService.class);
        serviceIntent.putExtra(Constants.PLAYLIST, playlist);
        startService(serviceIntent);
    }

}
