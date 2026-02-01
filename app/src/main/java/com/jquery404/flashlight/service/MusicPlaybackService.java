package com.jquery404.flashlight.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.media.app.NotificationCompat.MediaStyle;

import com.jquery404.flashlight.R;
import com.jquery404.flashlight.adapter.Song;
import com.jquery404.flashlight.main.MainActivity;
import com.jquery404.flashlight.main.MediaNotificationReceiver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MusicPlaybackService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {
        
    private static final String TAG = "MusicPlaybackService";
    private static final String CHANNEL_ID = "music_playback_channel";
    private static final int NOTIFICATION_ID = 1001;
    public static final String ACTION_STOP = "com.jquery404.flashlight.ACTION_STOP";
    
    // Playback Components
    private MediaPlayer mediaPlayer;
    private final IBinder binder = new MusicBinder();
    private PowerManager.WakeLock wakeLock;
    private AudioManager audioManager;
    private AudioFocusRequest audioFocusRequest;
    private AudioManager.OnAudioFocusChangeListener audioFocusListener;
    private MediaSessionCompat mediaSession;
    
    // State Management
    private List<Song> playlist = new ArrayList<>();
    private int currentIndex = -1;
    private PlaybackState currentState = PlaybackState.EMPTY;
    
    private final Handler handler = new Handler(Looper.getMainLooper());
    
    // Listener for UI updates
    public interface PlaybackStateListener {
        void onStateUpdated(PlaybackState state);
    }
    
    private PlaybackStateListener listener;
    
    public class MusicBinder extends Binder {
        public MusicPlaybackService getService() {
            return MusicPlaybackService.this;
        }
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        initAudioFocus();
        initMediaPlayer();
        initMediaSession();
        
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Flashlight::MusicWakeLock");
    }
    
    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnErrorListener(this);
    }
    
    private void initMediaSession() {
        mediaSession = new MediaSessionCompat(this, TAG);
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                play();
            }

            @Override
            public void onPause() {
                pause();
            }

            @Override
            public void onSkipToNext() {
                skipToNext();
            }

            @Override
            public void onSkipToPrevious() {
                skipToPrevious();
            }

            @Override
            public void onSeekTo(long pos) {
                seekTo(pos);
            }
            
            @Override
            public void onStop() {
                stopPlayback();
            }
        });
        mediaSession.setActive(true);
    }
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                if (ACTION_STOP.equals(action)) {
                    stopPlayback();
                    stopSelf();
                    return START_NOT_STICKY;
                }
                handleAction(action, intent);
            }
        }
        
        // Ensure notification is showing if we are running
        updateNotification();
        
        return START_STICKY;
    }
    
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopPlayback();
        stopSelf();
    }
    
    // ============================================================================================
    // Command Handling
    // ============================================================================================
    
    private void handleAction(String action, Intent intent) {
        if (MediaNotificationReceiver.ACTION_SEEK.equals(action)) {
            long seekPosition = intent.getLongExtra(MediaNotificationReceiver.EXTRA_SEEK_POSITION, 0);
            seekTo(seekPosition);
        } else if (MediaNotificationReceiver.ACTION_SEEK_TO_PERCENT.equals(action)) {
            // Deprecated/Legacy
        } else if (MediaNotificationReceiver.ACTION_PLAY.equals(action)) {
            play();
        } else if (MediaNotificationReceiver.ACTION_PAUSE.equals(action)) {
            pause();
        } else if (MediaNotificationReceiver.ACTION_NEXT.equals(action)) {
            skipToNext();
        } else if (MediaNotificationReceiver.ACTION_PREV.equals(action)) {
            skipToPrevious();
        }
    }
    
    // ============================================================================================
    // Public Control API
    // ============================================================================================
    
    public void playSong(List<Song> songs, int index) {
        if (songs == null || songs.isEmpty() || index < 0 || index >= songs.size()) return;
        
        this.playlist = new ArrayList<>(songs);
        this.currentIndex = index;
        Song song = playlist.get(currentIndex);
        
        startPlayback(song);
    }
    
    public void play() {
        Log.d(TAG, "play() called. Current state: " + currentState.getState());
        if (currentState.getState() == PlaybackState.State.PAUSED) {
            if (mediaPlayer != null) {
                if (requestAudioFocus()) {
                    Log.d(TAG, "Audio focus gained, starting playback");
                    mediaPlayer.start();
                    updateState(PlaybackState.State.PLAYING);
                } else {
                    Log.e(TAG, "Failed to gain audio focus");
                }
            }
        } else if (currentState.getState() == PlaybackState.State.STOPPED && currentIndex != -1) {
             Log.d(TAG, "Restarting playback from stopped state");
             startPlayback(playlist.get(currentIndex));
        }
    }
    
    public void pause() {
        Log.d(TAG, "pause() called");
        if (currentState.isPlaying()) {
            if (mediaPlayer != null) {
                Log.d(TAG, "Pausing playback");
                mediaPlayer.pause();
                updateState(PlaybackState.State.PAUSED);
                if (wakeLock != null && wakeLock.isHeld()) {
                    wakeLock.release();
                }
            }
        } else {
            Log.d(TAG, "pause() called but not playing. State: " + currentState.getState());
        }
    }
    
    public void seekTo(long position) {
        if (mediaPlayer != null) {
            long duration = mediaPlayer.getDuration();
            if (position < 0) position = 0;
            if (position > duration) position = duration;
            
            mediaPlayer.seekTo((int) position);
            updateState(currentState.getState());
        }
    }
    
    public void seekToPercent(int percent) {
       // Legacy
    }
    
    public void seekRelative(long deltaMs) {
        if (mediaPlayer != null) {
            long current = mediaPlayer.getCurrentPosition();
            seekTo(current + deltaMs);
        }
    }
    
    public void skipToNext() {
        Log.d(TAG, "skipToNext() called. Current index: " + currentIndex + ", playlist size: " + playlist.size());
        if (playlist.isEmpty()) {
            Log.e(TAG, "skipToNext: playlist is empty");
            return;
        }
        
        currentIndex++;
        if (currentIndex >= playlist.size()) {
            currentIndex = 0; 
        }
        
        Log.d(TAG, "skipToNext: Moving to index " + currentIndex);
        startPlayback(playlist.get(currentIndex));
    }
    
    public void skipToPrevious() {
        Log.d(TAG, "skipToPrevious() called. Current index: " + currentIndex + ", playlist size: " + playlist.size());
        if (playlist.isEmpty()) {
            Log.e(TAG, "skipToPrevious: playlist is empty");
            return;
        }
        
        // Check if we should restart current song (if > 3 seconds in)
        try {
            if (mediaPlayer != null && currentState.getState() == PlaybackState.State.PLAYING) {
                long position = mediaPlayer.getCurrentPosition();
                if (position > 3000) {
                    Log.d(TAG, "skipToPrevious: Restarting current song (position: " + position + ")");
                    seekTo(0);
                    return;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking position in skipToPrevious, continuing to prev song", e);
        }
        
        currentIndex--;
        if (currentIndex < 0) {
            currentIndex = playlist.size() - 1;
        }
        
        Log.d(TAG, "skipToPrevious: Moving to index " + currentIndex);
        startPlayback(playlist.get(currentIndex));
    }
    
    public void stopPlayback() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.reset();
        }
        abandonAudioFocus();
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
        updateState(PlaybackState.State.STOPPED);
        stopForeground(true);
        if (mediaSession != null) {
            mediaSession.setActive(false);
        }
    }
    
    // ============================================================================================
    // Internal Player Logic
    // ============================================================================================
    
    private void startPlayback(Song song) {
        try {
            if (mediaPlayer == null) initMediaPlayer();
            
            mediaPlayer.reset();
            Log.d(TAG, "Starting playback: " + song.getPath());
            mediaPlayer.setDataSource(song.getPath());
            mediaPlayer.prepareAsync();
            
            updateState(PlaybackState.State.BUFFERING);
            
        } catch (IOException e) {
            Log.e(TAG, "Error starting playback: " + e.getMessage());
            updateState(PlaybackState.State.ERROR);
        }
    }
    
    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.d(TAG, "onPrepared() called");
        if (requestAudioFocus()) {
            Log.d(TAG, "Audio focus granted, starting playback");
            mp.start();
            updateState(PlaybackState.State.PLAYING);
            Log.d(TAG, "MediaPlayer started, isPlaying: " + mp.isPlaying());
        } else {
            Log.e(TAG, "Failed to get audio focus in onPrepared");
            updateState(PlaybackState.State.PAUSED);
        }
    }
    
    @Override
    public void onCompletion(MediaPlayer mp) {
        skipToNext();
    }
    
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e(TAG, "MediaPlayer Error - what: " + what + ", extra: " + extra + ", song: " + 
            (currentState.getSong() != null ? currentState.getSong().getPath() : "null"));
        updateState(PlaybackState.State.ERROR);
        return true; 
    }
    
    // ============================================================================================
    // State & Notification Updates
    // ============================================================================================
    
    private void updateState(PlaybackState.State state) {
        Song currentSong = (currentIndex >= 0 && currentIndex < playlist.size()) 
                ? playlist.get(currentIndex) : null;
                
        long position = 0;
        long duration = 0;
        float playbackSpeed = 0f;
        int stateCompat = PlaybackStateCompat.STATE_NONE;
        
        if (mediaPlayer != null) {
            try {
                // Ensure we only query mediaPlayer if prepared
                boolean prepared = (state == PlaybackState.State.PLAYING || state == PlaybackState.State.PAUSED);
                if (prepared) {
                    position = mediaPlayer.getCurrentPosition();
                    duration = mediaPlayer.getDuration();
                }
            } catch (Exception e) {}
        }
        
        if (state == PlaybackState.State.PLAYING) {
            playbackSpeed = 1.0f;
            stateCompat = PlaybackStateCompat.STATE_PLAYING;
        } else if (state == PlaybackState.State.PAUSED) {
            stateCompat = PlaybackStateCompat.STATE_PAUSED;
        } else if (state == PlaybackState.State.BUFFERING) {
            stateCompat = PlaybackStateCompat.STATE_BUFFERING;
        } else if (state == PlaybackState.State.STOPPED) {
            stateCompat = PlaybackStateCompat.STATE_STOPPED;
        }
        
        currentState = new PlaybackState(
            state,
            currentSong,
            position,
            System.currentTimeMillis(),
            duration
        );
        
        // Update MediaSession PlaybackState
        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
            .setActions(PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PAUSE |
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                        PlaybackStateCompat.ACTION_SEEK_TO | PlaybackStateCompat.ACTION_STOP)
            .setState(stateCompat, position, playbackSpeed);
        mediaSession.setPlaybackState(stateBuilder.build());
        
        // Update MediaSession Metadata
        if (currentSong != null) {
            MediaMetadataCompat.Builder metaBuilder = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, currentSong.getName())
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, currentSong.getArtist())
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration);
                
            Bitmap art = getAlbumArt(currentSong.getPath());
            if (art != null) {
                metaBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, art);
            } else {
                 metaBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, 
                    BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
            }
            mediaSession.setMetadata(metaBuilder.build());
        }
        
        // Notify listener
        if (listener != null) {
            listener.onStateUpdated(currentState);
        }
        
        // Handle Wakelock
        if (state == PlaybackState.State.PLAYING) {
            if (wakeLock != null && !wakeLock.isHeld()) {
                wakeLock.acquire(10 * 60 * 1000L);
            }
        } else {
            if (wakeLock != null && wakeLock.isHeld()) {
                wakeLock.release();
            }
        }
        
        updateNotification();
    }
    
    private void updateNotification() {
        if (currentState.getSong() == null) return;
        
        Notification notification = buildNotification(currentState);
        
        if (currentState.isPlaying()) {
            startForeground(NOTIFICATION_ID, notification);
        } else {
            stopForeground(false);
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.notify(NOTIFICATION_ID, notification);
        }
    }
    
    // ============================================================================================
    // Helper Methods
    // ============================================================================================
    
    public void setListener(PlaybackStateListener listener) {
        this.listener = listener;
        if (listener != null) {
            listener.onStateUpdated(currentState);
        }
    }
    
    public PlaybackState getState() {
        return currentState;
    }
    
    private boolean requestAudioFocus() {
        Log.d(TAG, "requestAudioFocus() called");
        
        // Abandon any existing focus first to clean up old listeners
        abandonAudioFocus();
        
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
            
            audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(audioAttributes)
                .setOnAudioFocusChangeListener(audioFocusListener)
                .build();
            
            int result = audioManager.requestAudioFocus(audioFocusRequest);
            Log.d(TAG, "Audio focus request result: " + result);
            return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        } else {
            int result = audioManager.requestAudioFocus(audioFocusListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            Log.d(TAG, "Audio focus request result: " + result);
            return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        }
    }
    
    private void handleFocusChange(int focusChange) {
        Log.d(TAG, "Audio focus changed: " + focusChange);
         switch (focusChange) {
            case AudioManager.AUDIOFOCUS_LOSS:
                Log.d(TAG, "AUDIOFOCUS_LOSS - pausing");
                pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                Log.d(TAG, "AUDIOFOCUS_LOSS_TRANSIENT - pausing");
                pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                Log.d(TAG, "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK - ducking volume");
                if (mediaPlayer != null) mediaPlayer.setVolume(0.3f, 0.3f);
                break;
            case AudioManager.AUDIOFOCUS_GAIN:
                Log.d(TAG, "AUDIOFOCUS_GAIN - restoring volume");
                if (mediaPlayer != null) mediaPlayer.setVolume(1.0f, 1.0f);
                break;
        }
    }
    
    private void abandonAudioFocus() {
        if (audioManager == null) return;
        
        Log.d(TAG, "Abandoning audio focus");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (audioFocusRequest != null) {
                audioManager.abandonAudioFocusRequest(audioFocusRequest);
                audioFocusRequest = null;
            }
        } else {
            if (audioFocusListener != null) {
                audioManager.abandonAudioFocus(audioFocusListener);
            }
        }
    }
    
    private void initAudioFocus() {
        audioFocusListener = focusChange -> handleFocusChange(focusChange);
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Music Playback",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Music playback controls");
            channel.setShowBadge(false);
            channel.setSound(null, null);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
    
    private Notification buildNotification(PlaybackState state) {
        Song song = state.getSong();
        if (song == null) return buildEmptyNotification();
        
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        
        Bitmap art = getAlbumArt(song.getPath());
        if (art == null) {
            art = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        }

        int playPauseIcon = state.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play;
        String playPauseTitle = state.isPlaying() ? "Pause" : "Play";
        PendingIntent playPauseAction;
        if (state.isPlaying()) {
            playPauseAction = MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PAUSE);
        } else {
            playPauseAction = MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PLAY);
        }
        // Fallback to our custom receiver if MediaButton receiver isn't set up
        Intent ppIntent = new Intent(this, MediaNotificationReceiver.class);
        ppIntent.setAction(state.isPlaying() ? MediaNotificationReceiver.ACTION_PAUSE : MediaNotificationReceiver.ACTION_PLAY);
        playPauseAction = PendingIntent.getBroadcast(this, 1, ppIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        Intent prevIntent = new Intent(this, MediaNotificationReceiver.class);
        prevIntent.setAction(MediaNotificationReceiver.ACTION_PREV);
        PendingIntent prevPendingIntent = PendingIntent.getBroadcast(this, 2, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        Intent nextIntent = new Intent(this, MediaNotificationReceiver.class);
        nextIntent.setAction(MediaNotificationReceiver.ACTION_NEXT);
        PendingIntent nextPendingIntent = PendingIntent.getBroadcast(this, 3, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_flash_light)
            .setLargeIcon(art)
            .setContentTitle(song.getName())
            .setContentText(song.getArtist())
            .setContentIntent(contentIntent)
            .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_STOP))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(R.drawable.ic_play_prev, "Previous", prevPendingIntent)
            .addAction(playPauseIcon, playPauseTitle, playPauseAction)
            .addAction(R.drawable.ic_play_next, "Next", nextPendingIntent)
            .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSession.getSessionToken())
                .setShowActionsInCompactView(0, 1, 2))
            .build();
    }
    
    // Needed for MediaStyle
    public static class MediaButtonReceiver extends androidx.media.session.MediaButtonReceiver {
       // Placeholder if not defined elsewhere
    }
    
    private Notification buildEmptyNotification() {
         return new NotificationCompat.Builder(this, CHANNEL_ID)
             .setContentTitle("Flashlight")
              .setSmallIcon(R.drawable.ic_flash_light)
             .build();
    }
    
    private Bitmap getAlbumArt(String filePath) {
        if (filePath == null) return null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
            byte[] art = retriever.getEmbeddedPicture();
            if (art != null) {
              return BitmapFactory.decodeByteArray(art, 0, art.length);
            }
        } catch (Exception e) {
        } 
        return null;
    }
    
    public int getAudioSessionId() {
        if (mediaPlayer != null) {
            return mediaPlayer.getAudioSessionId();
        }
        return 0;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopPlayback();
        mediaSession.release();
    }
}
