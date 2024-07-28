package ru.krivocraft.tortoise.android.player;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.Optional;
import java.util.function.Consumer;

import ru.krivocraft.tortoise.core.api.Stamp;
import ru.krivocraft.tortoise.core.api.settings.WriteableSettings;

public final class ActualStamp {

    private final WriteableSettings settings;
    private final Consumer<String> error;
    private static final String KEY = "latest_queue";

    public ActualStamp(WriteableSettings settings, Consumer<String> error) {
        this.settings = settings;
        this.error = error;
    }

    public void persist(Stamp stamp) {
        settings.write(KEY, new Gson().toJson(stamp));
    }

    public void restore(Consumer<Stamp> executor) {
        Optional.ofNullable(settings.read(KEY, null)).flatMap(this::from).ifPresent(executor);
    }

    public void clear() {
        settings.reset(KEY);
    }

    private Optional<Stamp> from(String string) {
        try {
            return Optional.of(new Gson().fromJson(string, Stamp.class));
        } catch (JsonSyntaxException e) {
            error.accept(e.getMessage());
            return Optional.empty();
        }
    }

}
