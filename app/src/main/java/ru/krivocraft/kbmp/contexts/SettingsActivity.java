package ru.krivocraft.kbmp.contexts;

import android.os.Bundle;
import android.widget.ListView;

import java.util.Arrays;
import java.util.List;

import ru.krivocraft.kbmp.R;
import ru.krivocraft.kbmp.core.settings.SettingsAdapter;
import ru.krivocraft.kbmp.constants.Constants;

public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ListView listView = findViewById(R.id.settings_list);
        List<String> settings = getSettings();
        listView.setAdapter(new SettingsAdapter(this, settings));
    }

    private List<String> getSettings() {
        return Arrays.asList(Constants.KEY_THEME, Constants.KEY_SORT_BY_ARTIST, Constants.KEY_SORT_BY_TAG, Constants.KEY_RECOGNIZE_NAMES, Constants.KEY_CLEAR_CACHE);
    }
}
