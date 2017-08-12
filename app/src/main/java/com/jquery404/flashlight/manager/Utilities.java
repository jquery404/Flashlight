package com.jquery404.flashlight.manager;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.support.v4.content.ContextCompat;

import com.jquery404.flashlight.R;
import com.jquery404.flashlight.adapter.Song;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by vision on 7/24/2017.
 */

public class Utilities {
    private ArrayList<Song> songsList = new ArrayList<>();


    public String milliSecondsToTimer(long milliseconds) {
        String finalTimerString = "";
        String secondsString = "";

        // Convert total duration into time
        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        // Add hours if there
        if (hours > 0) {
            finalTimerString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        // return timer string
        return finalTimerString;
    }


    public int getProgressPercentage(long currentDuration, long totalDuration) {
        Double percentage = (double) 0;

        long currentSeconds = (int) (currentDuration / 1000);
        long totalSeconds = (int) (totalDuration / 1000);

        // calculating percentage
        percentage = (((double) currentSeconds) / totalSeconds) * 100;

        // return percentage
        return percentage.intValue();
    }


    public int progressToTimer(int progress, int totalDuration) {
        int currentDuration = 0;
        totalDuration = (int) (totalDuration / 1000);
        currentDuration = (int) ((((double) progress) / 100) * totalDuration);

        // return current duration in milliseconds
        return currentDuration * 1000;
    }


    public int cycleColor(float colorCounter) {
        int r = (int) Math.floor(128 * (Math.sin(colorCounter) + 3));
        int g = (int) Math.floor(128 * (Math.sin(colorCounter + 1) + 1));
        int b = (int) Math.floor(128 * (Math.sin(colorCounter + 7) + 1));
        return Color.argb(128, r, g, b);
    }

    public int getColorId(Context context) {
        int i = R.color.bit6;
        if (Math.random() * 10 < 3 && Math.random() * 10 > 5) {
            i = R.color.bit4;
        } else if (Math.random() * 10 < 5 && Math.random() * 10 > 7) {
            i = R.color.bit3;
        } else if (Math.random() * 10 < 7 && Math.random() * 10 > 9) {
            i = R.color.bit5;
        }
        return ContextCompat.getColor(context, i);
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
}
