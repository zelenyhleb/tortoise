package ru.krivocraft.kbmp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import java.util.ArrayList;

public class ExplorerFragment extends Fragment {

    public ExplorerFragment() {
        // Required empty public constructor
    }

    public static ExplorerFragment newInstance(ArrayList<TrackList> trackLists) {
        ExplorerFragment fragment = new ExplorerFragment();
        fragment.setTrackLists(trackLists);
        return fragment;
    }

    void setTrackLists(ArrayList<TrackList> trackLists) {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_explorer, container, false);
        final FragmentActivity context = getActivity();
        if (context != null) {
            GridView gridView = rootView.findViewById(R.id.playlists_grid);
            TracksAdapter adapter = new TracksAdapter(new ArrayList<>(), context);
            gridView.setAdapter(adapter);
            gridView.setOnItemClickListener((parent, view, position, id) -> {
                TracksFragment tracksFragment = new TracksFragment();
                tracksFragment.init(true, context, (TrackList) parent.getItemAtPosition(position));
                context.getSupportFragmentManager().beginTransaction().add(container.getId(), tracksFragment).commitAllowingStateLoss();
            });
        }
        return rootView;
    }


}
