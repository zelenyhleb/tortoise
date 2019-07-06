package ru.krivocraft.kbmp;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class SettingsPage extends Fragment {

    private Context context;
//    private SQLiteProcessor processor;

    public SettingsPage() {

    }

    void setContext(Context context){
        this.context = context;
//        this.processor = new SQLiteProcessor(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        ListView databaseView = rootView.findViewById(R.id.listdb);

//        List<Track> tracks = processor.readCompositions(null, null);
        List<String> trackDatas = new ArrayList<>();
//        for (Track track : tracks) {
//            trackDatas.add(track.getIdentifier() + " | " + track.getName() + " | " + track.getArtist() + " | " + track.getPath());
//        }
        ArrayAdapter arrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, trackDatas);
        databaseView.setAdapter(arrayAdapter);

        Button button = rootView.findViewById(R.id.button_clear_db);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                processor.clearDatabase();
            }
        });

        return rootView;
    }
}
