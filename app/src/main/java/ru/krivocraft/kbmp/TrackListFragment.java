package ru.krivocraft.kbmp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

public class TrackListFragment extends Fragment {

    private Playlist playlist;
    private AdapterView.OnItemClickListener listener;

    public TrackListFragment() {
        //required empty public constructor
    }

    void setData(Playlist playlist, AdapterView.OnItemClickListener listener) {
        this.listener = listener;
        this.playlist = playlist;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tracklist, container, false);

        final ListView listView = rootView.findViewById(R.id.fragment_track_list);
        listView.setAdapter(playlist.getTracksAdapter());
        listView.setOnItemClickListener(listener);

        EditText searchFrame = rootView.findViewById(R.id.search_edit_text);
        searchFrame.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Playlist playlistSearched = Utils.search(s, playlist);
                listView.setAdapter(playlistSearched.getTracksAdapter());
                if (s.length() == 0) {
                    listView.setAdapter(playlist.getTracksAdapter());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        return rootView;
    }
}
