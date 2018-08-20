package ru.krivocraft.kbmp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class PlaylistActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);
    }

    public void onClick(View view){
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra(Constants.COMPOSITION_NAME, "Flood On The Floor");
        intent.putExtra(Constants.COMPOSITION_AUTHOR, "Purity Ring");
        intent.putExtra(Constants.COMPOSITION_DURATION, "194");
        startActivity(intent);
    }
}
