package com.jquery404.flashlight.manager;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import androidx.core.content.ContextCompat;

import com.jquery404.flashlight.R;
import com.jquery404.flashlight.adapter.Song;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by vision on 7/24/2017.
 */

public class Utilities {

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
}
