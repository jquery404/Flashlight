package com.jquery404.flashlight.custom;

import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Debug;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.jquery404.flashlight.adapter.Song;

import java.io.File;
import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by faisal on 11/10/2017.
 */

public class SongManager {

    private ArrayList<Song> songsList = new ArrayList<>();
    private int currentSongPos = 0;

    public SongManager() {
        if (songsList == null)
            songsList = getPlayList();
    }

    public int getCurrentSongPos() {
        return currentSongPos;
    }

    public void setCurrentSongPos(int currentSongPos) {
        this.currentSongPos = currentSongPos;
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
            String title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            String bitrate = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);
            String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            String artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            if (artist == null)
                artist = "Unknown";
            if (title == null)
                title = song.getName().substring(0, (song.getName().length() - 4));

            long dur = Long.parseLong(duration);
            String seconds = String.valueOf((dur % 60000) / 1000);
            String minutes = String.valueOf(dur / 60000);
            if (seconds.length() == 1) {
                out = ("0" + minutes + ":0" + seconds);
            } else {
                out = ("0" + minutes + ":" + seconds);
            }
            Song mSong = new Song(title, song.getPath(), "" + (Integer.parseInt(bitrate)) / 1000, out, artist);

            songsList.add(mSong);
        }
    }

    public Song playNextSong() {
        Song song;

        if (currentSongPos < (songsList.size() - 1)) {
            song = songsList.get(currentSongPos + 1);
            currentSongPos = currentSongPos + 1;
        } else {
            song = songsList.get(0);
            currentSongPos = 0;
        }

        song.setNextSong(getNextSong(currentSongPos));

        return song;
    }

    public Song playPrevSong() {
        Song song;

        if (currentSongPos > 0) {
            song = songsList.get(currentSongPos - 1);
            currentSongPos = currentSongPos - 1;
        } else {
            song = songsList.get(songsList.size() - 1);
            currentSongPos = songsList.size() - 1;
        }

        song.setNextSong(getNextSong(currentSongPos));

        return song;
    }

    public String getNextSong(int index) {
        Song song;

        if (index < (songsList.size() - 1)) {
            song = songsList.get(index + 1);
        } else {
            song = songsList.get(0);
        }

        return song.getName();
    }
}
