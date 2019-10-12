package ru.krivocraft.kbmp.contexts;

import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.widget.ListView;

import java.util.Arrays;
import java.util.List;

import ru.krivocraft.kbmp.R;
import ru.krivocraft.kbmp.core.settings.SettingsAdapter;
import ru.krivocraft.kbmp.core.storage.SettingsStorageManager;

public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ListView listView = findViewById(R.id.settings_list);
        List<String> settings = getSettings();
        listView.setAdapter(new SettingsAdapter(this, settings));
    }

    @Override
    void init() {
        //Do nothing
    }

    @Override
    void onPlaybackStateChanged(PlaybackStateCompat newPlaybackState) {
        //Do nothing
    }

    @Override
    void onMetadataChanged(MediaMetadataCompat newMetadata) {
        //Do nothing
    }

    @Override
    void onMediaBrowserConnected() {
        //Do nothing
    }

    private List<String> getSettings() {
        return Arrays.asList(SettingsStorageManager.KEY_THEME, SettingsStorageManager.KEY_SORT_BY_ARTIST, SettingsStorageManager.KEY_SORT_BY_TAG, SettingsStorageManager.KEY_RECOGNIZE_NAMES, SettingsStorageManager.KEY_CLEAR_CACHE);
    }
}
