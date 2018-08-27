package ru.krivocraft.kbmp;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.File;
import java.util.List;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class PlaylistActivity extends AppCompatActivity {

    private ListView playlistView;

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

            PlayerService.LocalBinder binder = (PlayerService.LocalBinder) iBinder;
            final PlayerService service = binder.getServerInstance();

            playlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Composition composition = (Composition) adapterView.getItemAtPosition(i);

                    if (!composition.equals(service.getCurrentComposition())) {
                         service.newComposition(service.getCurrentPlaylist().indexOf(composition));
                    }

                    Intent intent = new Intent(PlaylistActivity.this, PlayerActivity.class);
                    startActivity(intent);
                }
            });

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    private Playlist playlist = new Playlist();
    private PlaylistAdapter playlistAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }

        RecursiveSearchTask searchTask = new RecursiveSearchTask();
        searchTask.execute(Environment.getExternalStorageDirectory());

        playlistView = findViewById(R.id.playlist);
        playlistAdapter = new PlaylistAdapter(playlist, this);
        playlistView.setAdapter(playlistAdapter);

        Intent serviceIntent = new Intent(this, PlayerService.class);
        serviceIntent.putExtra(Constants.PLAYLIST, playlist);
        startService(serviceIntent);

        bindService(serviceIntent, mConnection, BIND_ABOVE_CLIENT);
    }

    class RecursiveSearchTask extends AsyncTask<File, Void, List<Composition>> {
        @Override
        protected List<Composition> doInBackground(File... files) {
            return Utils.searchRecursively(files[0]);
        }

        @Override
        protected void onPostExecute(List<Composition> compositions) {
            super.onPostExecute(compositions);
            playlist.addCompositions(compositions);
            playlistAdapter.notifyDataSetChanged();
        }
    }

}
