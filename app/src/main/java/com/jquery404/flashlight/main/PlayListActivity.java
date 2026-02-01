package com.jquery404.flashlight.main;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jquery404.flashlight.R;
import com.jquery404.flashlight.adapter.PlaylistAdapter;
import com.jquery404.flashlight.adapter.Song;
import com.jquery404.flashlight.manager.Utilities;

import java.io.File;
import java.util.ArrayList;

import com.jquery404.flashlight.databinding.ActivityPlaylistBinding;

/**
 * Created by vision on 7/24/2017.
 */

public class PlayListActivity extends Activity {
    private ActivityPlaylistBinding binding;
    File sdCardRoot = Environment.getExternalStorageDirectory();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlaylistBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Utilities utilities = new Utilities();
        //ArrayList<Song> songsList = utilities.getPlayList();
        /*if (songsList != null) {
            binding.recyclerPlaylist.setLayoutManager(new LinearLayoutManager(this));
            RecyclerView.Adapter adapter = new PlaylistAdapter(this, songsList);
            binding.recyclerPlaylist.setAdapter(adapter);
            binding.recyclerPlaylist.setNestedScrollingEnabled(false);
        }*/

    }
}
