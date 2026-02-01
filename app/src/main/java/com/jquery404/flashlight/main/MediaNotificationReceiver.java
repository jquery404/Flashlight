package com.jquery404.flashlight.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.jquery404.flashlight.service.MusicPlaybackService;

public class MediaNotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "MediaNotificationReceiver";
    
    public static final String ACTION_PLAY = "com.jquery404.flashlight.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.jquery404.flashlight.ACTION_PAUSE";
    public static final String ACTION_NEXT = "com.jquery404.flashlight.ACTION_NEXT";
    public static final String ACTION_PREV = "com.jquery404.flashlight.ACTION_PREV";
    public static final String ACTION_SEEK = "com.jquery404.flashlight.ACTION_SEEK";
    public static final String ACTION_SEEK_FORWARD = "com.jquery404.flashlight.ACTION_SEEK_FORWARD";
    public static final String ACTION_SEEK_BACKWARD = "com.jquery404.flashlight.ACTION_SEEK_BACKWARD";
    public static final String ACTION_SEEK_TO_PERCENT = "com.jquery404.flashlight.ACTION_SEEK_TO_PERCENT";
    public static final String EXTRA_SEEK_POSITION = "seek_position";
    public static final String EXTRA_SEEK_PERCENT = "seek_percent";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) return;
        
        Log.d(TAG, "Received action: " + action);
        
        Intent serviceIntent = new Intent(context, MusicPlaybackService.class);
        serviceIntent.setAction(action);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }
}
