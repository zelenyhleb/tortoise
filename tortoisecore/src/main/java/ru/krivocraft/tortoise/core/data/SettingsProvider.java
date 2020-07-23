package ru.krivocraft.tortoise.core.data;

public interface SettingsProvider {

    boolean getOption(String key, boolean defaultValue);

    void putOption(String key, boolean value);
}
