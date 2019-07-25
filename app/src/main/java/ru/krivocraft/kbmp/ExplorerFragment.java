package ru.krivocraft.kbmp;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import java.util.ArrayList;

public class ExplorerFragment extends Fragment {

    private ArrayList<ArrayList<String>> trackLists;

    public ExplorerFragment() {
        // Required empty public constructor
    }

    public static ExplorerFragment newInstance(ArrayList<ArrayList<String>> trackLists) {
        ExplorerFragment fragment = new ExplorerFragment();
        fragment.setTrackLists(trackLists);
        return fragment;
    }

    void setTrackLists(ArrayList<ArrayList<String>> trackLists) {
        this.trackLists = trackLists;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_explorer, container, false);
        Context context = getContext();
        if (context != null) {
            GridView gridView = rootView.findViewById(R.id.playlists_grid);
            TrackListsAdapter adapter = new TrackListsAdapter(trackLists, context);
            gridView.setAdapter(adapter);
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                }
            });
        }
        return rootView;
    }


}
