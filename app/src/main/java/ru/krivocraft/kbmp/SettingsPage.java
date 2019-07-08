package ru.krivocraft.kbmp;

import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class SettingsPage extends Fragment {

    private Context context;
//    private SQLiteProcessor processor;

    public SettingsPage() {

    }

    void setContext(Context context){
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        Button button = rootView.findViewById(R.id.button_clear_db);
        button.setText("SWAP THEME");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences settings = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                if(settings.getBoolean("useAlternativeTheme", false)){
                    editor.putBoolean("useAlternativeTheme", false);
                } else {
                    editor.putBoolean("useAlternativeTheme", true);
                }
                editor.apply();
                Toast.makeText(context, "You can see changes after next launch", Toast.LENGTH_SHORT).show();
            }
        });

        return rootView;
    }
}
