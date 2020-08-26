package ru.krivocraft.tortoise.core.api.settings;

public abstract class WriteableSettings extends ReadOnlySettings {

    public abstract void write(String key, boolean value);

    public abstract void write(String key, int value);

    public abstract void write(String key, String value);

}
