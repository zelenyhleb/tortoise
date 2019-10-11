package ru.krivocraft.kbmp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.audiofx.Equalizer;

public class EqualizerManager {

    public static final String ACTION_REQUEST_STATE = "request_equalizer_state";
    public static final String ACTION_RESULT_STATE = "result_equalizer_state";
    public static final String ACTION_SET_BAND_LEVEL = "set_band_level";

    public static final String EXTRA_BAND = "band";
    public static final String EXTRA_BAND_LEVEL = "band_level";
    public static final String EXTRA_LEVEL = "gain";
    public static final String EXTRA_NUMBER_OF_BANDS = "number_bands";
    public static final String EXTRA_MIN_LEVEL = "equalizer_min_level";
    public static final String EXTRA_MAX_LEVEL = "equalizer_max_level";
    public static final String EXTRA_CENTER_FREQUENCY = "center_frequency";

    private final Equalizer equalizer;
    private final Context context;

    EqualizerManager(int audioSessionId, Context context) {
        this.equalizer = new Equalizer(0, audioSessionId);
        this.equalizer.setEnabled(true);
        this.context = context;

        registerReceivers();
    }

    private void registerReceivers() {
        IntentFilter dataRequestFilter = new IntentFilter(ACTION_REQUEST_STATE);
        context.registerReceiver(dataRequestReceiver, dataRequestFilter);

        IntentFilter setLevelFilter = new IntentFilter(ACTION_SET_BAND_LEVEL);
        context.registerReceiver(setLevelReceiver, setLevelFilter);
    }

    private void unregisterReceivers() {
        context.unregisterReceiver(dataRequestReceiver);
        context.unregisterReceiver(setLevelReceiver);
    }

    private void setBandGain(short band, short gain) {
        this.equalizer.setBandLevel(band, gain);
    }

    public void destroy() {
        unregisterReceivers();
    }

    private short getNumberOfBands() {
        return this.equalizer.getNumberOfBands();
    }

    private short getBandLevel(short band) {
        return equalizer.getBandLevel(band);
    }

    private short getMinEqualizerLevel() {
        return this.equalizer.getBandLevelRange()[0];
    }

    private short getMaxEqualizerLevel() {
        return this.equalizer.getBandLevelRange()[1];
    }

    private int getCenterFrequency(short band) {
        return this.equalizer.getCenterFreq(band);
    }

    private final BroadcastReceiver dataRequestReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent resultIntent = new Intent(ACTION_RESULT_STATE);
            resultIntent.putExtra(EXTRA_MIN_LEVEL, getMinEqualizerLevel());
            resultIntent.putExtra(EXTRA_MAX_LEVEL, getMaxEqualizerLevel());
            resultIntent.putExtra(EXTRA_NUMBER_OF_BANDS, getNumberOfBands());
            for (short i = 0; i < getNumberOfBands(); i++) {
                resultIntent.putExtra(EXTRA_CENTER_FREQUENCY + i, getCenterFrequency(i));
                resultIntent.putExtra(EXTRA_BAND_LEVEL + i, getBandLevel(i));
            }
            context.sendBroadcast(resultIntent);
        }
    };

    private final BroadcastReceiver setLevelReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            short band = intent.getShortExtra(EXTRA_BAND, (short) 0);
            short level = intent.getShortExtra(EXTRA_LEVEL, (short) 0);
            setBandGain(band, level);
        }
    };
}
