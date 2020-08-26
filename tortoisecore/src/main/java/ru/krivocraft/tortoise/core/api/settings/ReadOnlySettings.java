package ru.krivocraft.tortoise.core.api.settings;

public abstract class ReadOnlySettings implements Settings {

    public abstract boolean read(String key, boolean defaultValue);

    public abstract int read(String key, int defaultValue);

    public abstract String read(String key, String defaultValue);
}
