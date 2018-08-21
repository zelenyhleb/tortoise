package ru.krivocraft.kbmp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class PlaylistActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
    }

    public void onClick(View view){
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra(Constants.COMPOSITION_NAME, "Flood On The Floor");
        intent.putExtra(Constants.COMPOSITION_AUTHOR, "Purity Ring");
        intent.putExtra(Constants.COMPOSITION_DURATION, "194");
        intent.putExtra(Constants.COMPOSITION_LOCATION, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC) + "/music.mp3");
        startActivity(intent);
    }
}
