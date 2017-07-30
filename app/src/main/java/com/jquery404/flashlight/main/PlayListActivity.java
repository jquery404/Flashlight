package com.jquery404.flashlight.main;

import android.app.Activity;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.jquery404.flashlight.R;
import com.jquery404.flashlight.adapter.PlaylistAdapter;
import com.jquery404.flashlight.adapter.Song;

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
    private ArrayList<Song> songsList = new ArrayList<>();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        ButterKnife.bind(this);
        songsList = getPlayList();
        if (songsList != null) {
            recycler_playlist.setLayoutManager(new LinearLayoutManager(this));
            RecyclerView.Adapter adapter = new PlaylistAdapter(this, songsList);
            recycler_playlist.setAdapter(adapter);
            recycler_playlist.setNestedScrollingEnabled(false);
        }

    }


    public ArrayList<Song> getPlayList() {
        File home = Environment.getExternalStorageDirectory();
        File[] listFiles = home.listFiles();
        if (listFiles != null && listFiles.length > 0) {
            for (File file : listFiles) {
                if (file.isDirectory()) {
                    scanDirectory(file);
                } else {
                    addSongToList(file);
                }
            }
        }

        return songsList;
    }

    private void scanDirectory(File directory) {
        if (directory != null) {
            File[] listFiles = directory.listFiles();
            if (listFiles != null && listFiles.length > 0) {
                for (File file : listFiles) {
                    if (file.isDirectory()) {
                        scanDirectory(file);
                    } else {
                        addSongToList(file);
                    }
                }
            }
        }
    }

    private void addSongToList(File song) {
        String out = "";

        if (song.getName().endsWith(".ogg") ||
                song.getName().endsWith(".mp3") ||
                song.getName().endsWith(".MP3")) {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(song.getPath());

            String bitrate = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);
            String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long dur = Long.parseLong(duration);
            String seconds = String.valueOf((dur % 60000) / 1000);
            String minutes = String.valueOf(dur / 60000);
            if (seconds.length() == 1) {
                out = ("0" + minutes + ":0" + seconds);
            } else {
                out = ("0" + minutes + ":" + seconds);
            }
            Song mSong = new Song(song.getName().substring(0, (song.getName().length() - 4)),
                    song.getPath(), "" + (Integer.parseInt(bitrate)) / 1000, out);

            songsList.add(mSong);
        }
    }

}
