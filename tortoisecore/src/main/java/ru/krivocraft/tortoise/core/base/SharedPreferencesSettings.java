package ru.krivocraft.tortoise.core.base;

import ru.krivocraft.tortoise.core.api.settings.Settings;
import ru.krivocraft.tortoise.core.api.settings.WriteableSettings;

public class SharedPreferencesSettings extends WriteableSettings {


    @Override
    public boolean read(String key, boolean defaultValue) {
        return false;
    }

    @Override
    public int read(String key, int defaultValue) {
        return 0;
    }

    @Override
    public String read(String key, String defaultValue) {
        return null;
    }

    @Override
    public void write(String key, boolean value) {

    }

    @Override
    public void write(String key, int value) {

    }

    @Override
    public void write(String key, String value) {

    }
}
