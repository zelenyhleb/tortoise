package ru.krivocraft.kbmp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class PlaylistActivity extends AppCompatActivity {

    private ListView playlist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
        playlist = findViewById(R.id.playlist);
        playlist.setAdapter(new CompositionAdapter(this, Utils.search(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC))));
        playlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Composition composition = (Composition) adapterView.getItemAtPosition(i);
                Intent intent = new Intent(PlaylistActivity.this, PlayerActivity.class);
                intent.putExtra(Constants.COMPOSITION_NAME, composition.getName());
                intent.putExtra(Constants.COMPOSITION_AUTHOR, composition.getComposer());
                intent.putExtra(Constants.COMPOSITION_DURATION, composition.getDuration());
                intent.putExtra(Constants.COMPOSITION_LOCATION, composition.getPath());
                startActivity(intent);
            }
        });

        startService(new Intent(this, PlayerService.class));
    }

    private class CompositionAdapter extends ArrayAdapter<Composition> {

        public CompositionAdapter(@NonNull Context context, @NonNull List<Composition> objects) {
            super(context, R.layout.composition_list_item, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            Composition composition = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.composition_list_item, null);
            }

            ((TextView) convertView.findViewById(R.id.composition_name_text)).setText(composition.getName());
            ((TextView) convertView.findViewById(R.id.composition_author_text)).setText(composition.getComposer());

            return convertView;
        }
    }

}
