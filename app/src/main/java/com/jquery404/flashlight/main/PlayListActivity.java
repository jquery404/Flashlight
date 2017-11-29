package com.jquery404.flashlight.main;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.jquery404.flashlight.R;
import com.jquery404.flashlight.adapter.PlaylistAdapter;
import com.jquery404.flashlight.adapter.Song;
import com.jquery404.flashlight.manager.Utilities;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by vision on 7/24/2017.
 */

public class PlayListActivity extends Activity {

    @BindView(R.id.recycler_playlist)
    RecyclerView recycler_playlist;

    File sdCardRoot = Environment.getExternalStorageDirectory();


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        ButterKnife.bind(this);
        Utilities utilities = new Utilities();
        //ArrayList<Song> songsList = utilities.getPlayList();
        /*if (songsList != null) {
            recycler_playlist.setLayoutManager(new LinearLayoutManager(this));
            RecyclerView.Adapter adapter = new PlaylistAdapter(this, songsList);
            recycler_playlist.setAdapter(adapter);
            recycler_playlist.setNestedScrollingEnabled(false);
        }*/

    }


}
