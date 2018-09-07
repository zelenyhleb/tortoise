package ru.krivocraft.kbmp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

public class PlayerFragment extends Fragment implements View.OnClickListener {

    private ImageButton playPauseCompositionButton;
    private OnClickListener listener;

    public PlayerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_player, container, false);

        ImageButton previousCompositionButton = rootView.findViewById(R.id.fragment_button_previous);
        ImageButton nextCompositionButton = rootView.findViewById(R.id.fragment_button_next);

        playPauseCompositionButton = rootView.findViewById(R.id.fragment_button_playpause);

        previousCompositionButton.setOnClickListener(this);
        nextCompositionButton.setOnClickListener(this);

        playPauseCompositionButton.setOnClickListener(this);

        return rootView;
    }

    public void setListener(OnClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onClick(View v) {
        listener.onClick(v);
    }

    interface OnClickListener {
        void onClick(View v);
    }
}
