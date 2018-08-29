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
import java.util.Set;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class PlaylistActivity extends AppCompatActivity {

    private boolean mBounded = false;

    private PlaylistAdapter mPlaylistAdapter;
    private PlayerService mService;

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mBounded = true;

            PlayerService.LocalBinder binder = (PlayerService.LocalBinder) iBinder;
            mService = binder.getServerInstance();

            ListView playlistView = findViewById(R.id.playlist);

            mPlaylistAdapter = new PlaylistAdapter(mService.getCurrentPlaylist(), PlaylistActivity.this);
            playlistView.setAdapter(mPlaylistAdapter);

            playlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Composition composition = (Composition) adapterView.getItemAtPosition(i);

                    if (!composition.equals(mService.getCurrentComposition())) {
                        mService.newComposition(mService.getCurrentPlaylist().indexOf(composition));
                    }

                    Intent intent = new Intent(PlaylistActivity.this, PlayerActivity.class);
                    startActivity(intent);
                }
            });

            loadCompositions();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBounded = false;
        }
    };

    private void loadCompositions() {
        if (mBounded) {
            Playlist playlist = mService.getCurrentPlaylist();
            Set<String> paths = Utils.getPaths(PlaylistActivity.this);
            for (String path : paths) {
                if (!playlist.contains(path)) {
                    playlist.addComposition(Utils.getComposition(new File(path), playlist.getSize()));
                }
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }

        RecursiveSearchTask searchTask = new RecursiveSearchTask();
        searchTask.execute(Environment.getExternalStorageDirectory());

        Intent serviceIntent = new Intent(this, PlayerService.class);
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
            for (Composition composition : compositions) {
                Utils.putPath(PlaylistActivity.this, composition.getPath());
            }
            loadCompositions();
            mPlaylistAdapter.notifyDataSetChanged();
        }
    }

}
