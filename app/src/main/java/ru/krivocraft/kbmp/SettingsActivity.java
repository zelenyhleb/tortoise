package ru.krivocraft.kbmp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    public void onClick(View v) {
        new SQLiteProcessor(this).clearDatabase();
        supportFinishAfterTransition();
    }
}
