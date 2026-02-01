package com.jquery404.flashlight.main;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.jquery404.flashlight.R;
import com.jquery404.flashlight.adapter.Song;

public class MediaNotificationManager {
    private static final String TAG = "MediaNotificationManager";
    private static final String CHANNEL_ID = "music_playback_channel";
    private static final int NOTIFICATION_ID = 1001;
    
    private Context context;
    private NotificationManager notificationManager;
    private Song currentSong;
    private boolean isPlaying = false;
    private long currentPosition = 0;
    private long duration = 0;
    
    public MediaNotificationManager(Context context) {
        this.context = context;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Music Playback",
                NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Music playback controls");
            channel.setShowBadge(false);
            channel.setSound(null, null);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
    
    public void updateNotification(Song song, boolean playing, long position, long totalDuration) {
        if (song == null) {
            return;
        }
        
        currentSong = song;
        isPlaying = playing;
        currentPosition = position;
        duration = totalDuration;
        
        Notification notification = buildNotification(song, playing, position, totalDuration);
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, notification);
        }
    }
    
    private Notification buildNotification(Song song, boolean playing, long position, long totalDuration) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        Intent playPauseIntent = new Intent(context, MediaNotificationReceiver.class);
        playPauseIntent.setAction(playing ? MediaNotificationReceiver.ACTION_PAUSE : MediaNotificationReceiver.ACTION_PLAY);
        PendingIntent playPausePendingIntent = PendingIntent.getBroadcast(
            context, 0, playPauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        Intent nextIntent = new Intent(context, MediaNotificationReceiver.class);
        nextIntent.setAction(MediaNotificationReceiver.ACTION_NEXT);
        PendingIntent nextPendingIntent = PendingIntent.getBroadcast(
            context, 0, nextIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        Intent prevIntent = new Intent(context, MediaNotificationReceiver.class);
        prevIntent.setAction(MediaNotificationReceiver.ACTION_PREV);
        PendingIntent prevPendingIntent = PendingIntent.getBroadcast(
            context, 0, prevIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        Bitmap albumArt = getAlbumArt(song.getPath());
        Bitmap largeIcon = albumArt != null ? albumArt : 
            BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setLargeIcon(largeIcon)
            .setContentTitle(song.getName())
            .setContentText(song.getArtist())
            .setContentIntent(contentIntent)
            .setOngoing(playing)
            .setShowWhen(false)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0, 1, 2))
            .addAction(R.drawable.ic_play_prev_ds, "Previous", prevPendingIntent)
            .addAction(
                playing ? R.drawable.ic_pause : R.drawable.ic_play,
                playing ? "Pause" : "Play",
                playPausePendingIntent
            )
            .addAction(R.drawable.ic_play_next_ds, "Next", nextPendingIntent);
        
        if (totalDuration > 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setProgress((int) totalDuration, (int) position, false);
        }
        
        return builder.build();
    }
    
    private Bitmap getAlbumArt(String filePath) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
            byte[] albumArt = retriever.getEmbeddedPicture();
            if (albumArt != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(albumArt, 0, albumArt.length);
                if (bitmap != null) {
                    return scaleBitmap(bitmap, 512, 512);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error extracting album art", e);
        } finally {
            try {
                retriever.release();
            } catch (Exception e) {
                Log.e(TAG, "Error releasing MediaMetadataRetriever", e);
            }
        }
        return null;
    }
    
    private Bitmap scaleBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        
        if (width <= maxWidth && height <= maxHeight) {
            return bitmap;
        }
        
        float scale = Math.min((float) maxWidth / width, (float) maxHeight / height);
        int newWidth = Math.round(width * scale);
        int newHeight = Math.round(height * scale);
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }
    
    public void cancelNotification() {
        if (notificationManager != null) {
            notificationManager.cancel(NOTIFICATION_ID);
        }
    }
}
