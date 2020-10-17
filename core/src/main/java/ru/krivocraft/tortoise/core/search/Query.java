package ru.krivocraft.tortoise.core.search;

import java.util.function.Predicate;

final class Query implements Predicate<Entry> {

    private final String body;

    Query(String query) {
        this.body = query.toLowerCase();
    }

    @Override
    public boolean test(Entry entry) {
        return entry.value().contains(body);
    }
}
