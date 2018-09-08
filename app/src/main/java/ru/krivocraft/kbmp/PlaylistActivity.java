package ru.krivocraft.kbmp;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.List;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class PlaylistActivity extends AppCompatActivity implements Composition.OnCompositionStateChangedListener {

    private boolean mBounded = false;

    private PlaylistAdapter mPlaylistAdapter;
    private PlayerService mService;

    private SQLiteProcessor database;

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mBounded = true;

            PlayerService.LocalBinder binder = (PlayerService.LocalBinder) iBinder;
            mService = binder.getServerInstance();
            mService.addListener(PlaylistActivity.this);

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

            refreshFragment();
            loadCompositions();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBounded = false;
        }
    };
    private int PERMISSION_WRITE_EXTERNAL_STORAGE = 0;
    private PlayerFragment fragment;

    private void loadCompositions() {
        if (mBounded) {
            Playlist playlist = mService.getCurrentPlaylist();
            List<Composition> compositions = database.readCompositions();
            for (Composition composition : compositions) {
                if (!playlist.contains(composition)) {
                    playlist.addComposition(composition);
                }
            }
            for (Composition composition : database.readCompositions()) {
                System.out.println(composition.getIdentifier() + ":" + composition.getAuthor() + ":" + composition.getName() + ":" + composition.getPath());
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_WRITE_EXTERNAL_STORAGE);
            return;
        }
        initPlaylist();
    }

    private void initPlaylist() {
        database = new SQLiteProcessor(this);

        RecursiveSearchTask searchTask = new RecursiveSearchTask();
        searchTask.execute(Environment.getExternalStorageDirectory());

        Intent serviceIntent = new Intent(this, PlayerService.class);
        startService(serviceIntent);

        bindService(serviceIntent, mConnection, BIND_ABOVE_CLIENT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initPlaylist();
            } else {
                Toast.makeText(this, "App needs external storage permission to work", Toast.LENGTH_LONG).show();
            }
        }
    }

    private Composition.OnCompositionFoundListener onCompositionFoundListener = new Composition.OnCompositionFoundListener() {
        @Override
        public void onCompositionFound(Composition composition) {
            database.writeComposition(composition);
            loadCompositions();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mPlaylistAdapter.notifyDataSetChanged();
                }
            });
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        refreshFragment();
    }

    private void refreshFragment() {
        hideFragment();
        showFragment();
    }

    private void showFragment() {
        if (mBounded) {
            Composition composition = mService.getCurrentComposition();
            if (composition != null) {
                fragment = new PlayerFragment();
                int progress = Utils.getSeconds(mService.getProgress());
                int duration = Utils.getSeconds(Integer.parseInt(composition.getDuration()));
                fragment.setData(composition.getAuthor(), composition.getName(), progress, duration, mService.isPlaying());
                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.container, fragment)
                        .commit();
            }
        }
    }

    private void hideFragment() {
        if (fragment != null) {
            fragment.destroy();
            getSupportFragmentManager()
                    .beginTransaction()
                    .remove(fragment)
                    .commit();
            fragment = null;
        }
    }

    @Override
    public void onNewComposition() {
        refreshFragment();
    }

    @Override
    public void onPlayComposition() {
        refreshFragment();
    }

    @Override
    public void onPauseComposition() {
        refreshFragment();
    }

    class RecursiveSearchTask extends AsyncTask<File, Void, Void> {
        @Override
        protected Void doInBackground(File... files) {
            Utils.searchRecursively(files[0], onCompositionFoundListener);
            return null;
        }
    }

}
