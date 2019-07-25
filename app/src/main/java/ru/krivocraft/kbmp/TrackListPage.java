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
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

public class TrackListPage extends AbstractTrackViewFragment {

    private ArrayList<String> trackList;
    private TracksAdapter adapter;
    private ListView listView;
    private boolean showControls;
    private ProgressBar progressBar;

    public TrackListPage() {
        super();
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateData(context, intent);
        }
    };

    private void updateData(final Context context, Intent intent) {
        TrackListPage.this.trackList = intent.getStringArrayListExtra(Constants.EXTRA_TRACK_LIST);

        LoadDataTask loadDataTask = new LoadDataTask();
        loadDataTask.setContentResolver(context.getContentResolver());
        loadDataTask.setCallback(new LoadDataTask.DataLoaderCallback() {
            @Override
            public void onDataLoaded(List<Track> track) {
                TrackListPage.this.adapter = new TracksAdapter(track, context);
                if (listView != null) {
                    TrackListPage.this.listView.setAdapter(adapter);
                }
                progressBar.setVisibility(View.INVISIBLE);
                invalidate();
            }
        });
        loadDataTask.execute(trackList.toArray(new String[0]));
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

    ArrayList<String> getTrackList() {
        return trackList;
    }

    void init(boolean showControls, Context context) {
        this.showControls = showControls;
        this.trackList = new ArrayList<>();
        this.adapter = new TracksAdapter(new ArrayList<Track>(), context);

        IntentFilter filter = new IntentFilter(Constants.ACTION_UPDATE_TRACK_LIST);
        context.registerReceiver(receiver, filter);

        Intent intent = new Intent(Constants.ACTION_REQUEST_TRACK_LIST);
        context.sendBroadcast(intent);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tracklist, container, false);

        progressBar = rootView.findViewById(R.id.track_list_progress_bar);

        listView = rootView.findViewById(R.id.fragment_track_list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent serviceIntent = new Intent(Constants.ACTION_PLAY_FROM_LIST);
                serviceIntent.putExtra(Constants.EXTRA_PATH, ((Track) parent.getItemAtPosition(position)).getPath());
                view.getContext().sendBroadcast(serviceIntent);

                Intent interfaceIntent = new Intent(Constants.ACTION_SHOW_PLAYER);
                view.getContext().sendBroadcast(interfaceIntent);
            }
        });

        EditText searchFrame = rootView.findViewById(R.id.search_edit_text);
        ImageButton buttonShuffle = rootView.findViewById(R.id.shuffle);

        if (showControls) {
            searchFrame.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
//                    Context context = getContext();
//                    if (context != null) {
//                        ArrayList<String> trackListSearched = Utils.search(s, trackList, context.getContentResolver());
//                        listView.setAdapter(new TracksAdapter(trackListSearched, context));
//                        if (s.length() == 0) {
//                            listView.setAdapter(adapter);
//                        }
//                    }
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
