package com.jquery404.flashlight.helper;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;

import com.jquery404.flashlight.R;

/**
 * Created by Faisal on 7/26/17.
 */

public class Utils {

    public String milliSecondsToTimer(long milliseconds) {
        String finalTimerString = "";
        String secondsString = "";

        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        if (hours > 0) {
            finalTimerString = hours + ":";
        }

        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        return finalTimerString;
    }

    public int getProgressPercentage(long currentDuration, long totalDuration) {
        Double percentage = (double) 0;

        long currentSeconds = (int) (currentDuration / 1000);
        long totalSeconds = (int) (totalDuration / 1000);

        percentage = (((double) currentSeconds) / totalSeconds) * 100;

        return percentage.intValue();
    }

    public int progressToTimer(int progress, int totalDuration) {
        int currentDuration = 0;
        totalDuration = (int) (totalDuration / 1000);
        currentDuration = (int) ((((double) progress) / 100) * totalDuration);

        return currentDuration * 1000;
    }

    public static int cycleColor(float colorCounter) {
        int r = (int) Math.floor(128 * (Math.sin(colorCounter) + 3));
        int g = (int) Math.floor(128 * (Math.sin(colorCounter + 1) + 1));
        int b = (int) Math.floor(128 * (Math.sin(colorCounter + 7) + 1));
        return Color.argb(128, r, g, b);
    }

    public static int getColorId(Context context) {
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
