package ru.krivocraft.kbmp.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import ru.krivocraft.kbmp.core.audiofx.EqualizerManager;
import ru.krivocraft.kbmp.R;

public class EqualizerFragment extends Fragment {

    public static final String ACTION_RESULT_SESSION_ID = "result_session_id";

    private LinearLayout linearLayout;

    public EqualizerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_equalizer, container, false);
        linearLayout = rootView.findViewById(R.id.equalizer_layout);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Context context = getContext();
        if (context != null) {
            context.registerReceiver(receiver, new IntentFilter(EqualizerManager.ACTION_RESULT_STATE));
            context.sendBroadcast(new Intent(EqualizerManager.ACTION_REQUEST_STATE));
        }
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            linearLayout.removeAllViews();
            short numberOfBands = intent.getShortExtra(EqualizerManager.EXTRA_NUMBER_OF_BANDS, (short) 0);

            final short minEQLevel = intent.getShortExtra(EqualizerManager.EXTRA_MIN_LEVEL, (short) 0);
            final short maxEQLevel = intent.getShortExtra(EqualizerManager.EXTRA_MAX_LEVEL, (short) 0);

            for (short i = 0; i < numberOfBands; i++) {
                final short band = i;
                final short bandLevel = intent.getShortExtra(EqualizerManager.EXTRA_BAND_LEVEL + i, (short) 0);
                final int centerFrequency = intent.getIntExtra(EqualizerManager.EXTRA_CENTER_FREQUENCY + i, 0);

                TextView freqTextView = new TextView(context);
                freqTextView.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.FILL_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                freqTextView.setGravity(Gravity.CENTER_HORIZONTAL);
                freqTextView.setText((centerFrequency / 1000) + " Hz");
                linearLayout.addView(freqTextView);

                LinearLayout row = new LinearLayout(context);
                row.setOrientation(LinearLayout.HORIZONTAL);

                TextView minDbTextView = new TextView(context);
                minDbTextView.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                minDbTextView.setText((minEQLevel / 100) + " dB");

                TextView maxDbTextView = new TextView(context);
                maxDbTextView.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                maxDbTextView.setText((maxEQLevel / 100) + " dB");

                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.FILL_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.weight = 1;
                SeekBar bar = new SeekBar(context);
                bar.setLayoutParams(layoutParams);
                bar.setMax(maxEQLevel - minEQLevel);
                bar.setProgress(bandLevel - minEQLevel);

                bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser) {
                            Intent resultIntent = new Intent(EqualizerManager.ACTION_SET_BAND_LEVEL);
                            resultIntent.putExtra(EqualizerManager.EXTRA_BAND, band);
                            resultIntent.putExtra(EqualizerManager.EXTRA_LEVEL, (short) (seekBar.getProgress() + minEQLevel));
                            getContext().sendBroadcast(resultIntent);
                        }
                    }

                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });

                row.addView(minDbTextView);
                row.addView(bar);
                row.addView(maxDbTextView);

                linearLayout.addView(row);
            }

        }
    };
}
