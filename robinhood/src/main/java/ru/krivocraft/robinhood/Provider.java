package ru.krivocraft.robinhood;

import java.util.List;

import ru.krivocraft.robinhood.model.Audio;

public interface Provider {

    List<Audio> getAllAvailableTracks();
}
