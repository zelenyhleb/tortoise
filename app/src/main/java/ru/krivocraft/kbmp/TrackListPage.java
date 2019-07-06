package ru.krivocraft.kbmp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import java.util.ArrayList;

public class TrackListPage extends AbstractTrackViewFragment {

    private ArrayList<String> trackList;
    private AdapterView.OnItemClickListener listener;
    private TracksAdapter adapter;
    private ListView listView;
    private boolean showControls;

    public TrackListPage() {
        super();
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateData(context, intent);
        }
    };

    private void updateData(Context context, Intent intent) {
        ArrayList<String> tracks = intent.getStringArrayListExtra(Constants.EXTRA_TRACK_LIST);
        TrackListPage.this.trackList = tracks;
        TrackListPage.this.adapter = new TracksAdapter(tracks, context);
        if (listView != null) {
            TrackListPage.this.listView.setAdapter(adapter);
        }
        invalidate();
    }

    @Override
    void invalidate() {
        if (listView != null) {
            listView.invalidateViews();
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    void init(AdapterView.OnItemClickListener listener, boolean showControls, Context context) {
        this.listener = listener;
        this.showControls = showControls;
        this.trackList = new ArrayList<>();
        this.adapter = new TracksAdapter(trackList, context);

        IntentFilter filter = new IntentFilter(Constants.ACTION_UPDATE_TRACK_LIST);
        context.registerReceiver(receiver, filter);

        Intent intent = new Intent(Constants.ACTION_REQUEST_TRACK_LIST);
        context.sendBroadcast(intent);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tracklist, container, false);

        listView = rootView.findViewById(R.id.fragment_track_list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(listener);

        EditText searchFrame = rootView.findViewById(R.id.search_edit_text);
        ImageButton buttonShuffle = rootView.findViewById(R.id.shuffle);

        if (showControls) {
            searchFrame.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    Context context = getContext();
                    if (context != null) {
                        ArrayList<String> trackListSearched = Utils.search(s, trackList, context.getContentResolver());
                        listView.setAdapter(new TracksAdapter(trackListSearched, context));
                        if (s.length() == 0) {
                            listView.setAdapter(adapter);
                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
            buttonShuffle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = getContext();
                    if (context != null) {
                        context.sendBroadcast(new Intent(Constants.ACTION_SHUFFLE));
                    }
                    adapter.notifyDataSetChanged();
                }
            });
        } else {
            searchFrame.setVisibility(View.INVISIBLE);
            buttonShuffle.setVisibility(View.INVISIBLE);
        }


        return rootView;
    }
}
