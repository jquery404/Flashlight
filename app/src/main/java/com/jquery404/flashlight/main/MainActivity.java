package com.jquery404.flashlight.main;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatImageView;

import com.jquery404.flashlight.R;
import com.jquery404.flashlight.adapter.Song;
import com.jquery404.flashlight.custom.AboutDialog;
import com.jquery404.flashlight.custom.CircularSeekBar;
import com.jquery404.flashlight.custom.DotCircularProgressBar;
import com.jquery404.flashlight.custom.OnSongSelectedListener;
import com.jquery404.flashlight.custom.SongListDialog;
import com.jquery404.flashlight.custom.SongManager;
import com.jquery404.flashlight.databinding.ActivityMainBinding;
import com.jquery404.flashlight.manager.Utilities;
import com.jquery404.flashlight.service.MusicPlaybackService;
import com.jquery404.flashlight.service.PlaybackState;

import java.util.ArrayList;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by Faisal on 6/30/17.
 */

public class MainActivity extends BaseCompatActivity implements
        Visualizer.OnDataCaptureListener, OnSongSelectedListener, EasyPermissions.PermissionCallbacks,
        MusicPlaybackService.PlaybackStateListener {

    private ActivityMainBinding binding;
    
    private VisualizerView visualizerView;
    private int lastAudioSessionId = -1;
    private CircularVisualizerView circularVisualizer;
    private TextView tvSongTitle;
    private TextView tvBPM;
    private TextView tvHeaderSongName;
    private TextView tvHeaderArtistName;
    private ImageView ivAlbumArt;
    private View backgroundBeat;
    private View soundPlate;
    private View mCroller;
    private View circleViewWrapper;
    private View btnBrowser;
    private View progressBar;
    private ProgressBarHandler progressBarHandler; 
    
    private AppCompatImageView btnFlash;
    private AppCompatImageView btnPlay;
    private AppCompatImageView btnNext;
    private AppCompatImageView btnPrev;

    private static final int REQUEST_PATH = 121;
    private static final int REQUEST_CAMERA_MIC_STORAGE = 122;
    private static final int REQUEST_NOTIFICATION_PERMISSION = 123;
    
    private String[] getRequiredPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.READ_MEDIA_AUDIO};
        } else {
            return new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.READ_EXTERNAL_STORAGE};
        }
    }

    private enum PlayState {
        PLAY, PAUSE, STOP, DISABLE
    }

    private Visualizer mVisualizer;
    private BeatDetector beatDetector;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private android.animation.ObjectAnimator rotationAnimator;
    private Utilities utils;
    private SongManager songManager;
    private FlashLightManager flashLightManager;
    private boolean useFlash = true;
    private ArrayList<Song> songsList;
    private SongListDialog songListDialog;
    boolean doubleBackToExitPressedOnce = false;
    
    private MusicPlaybackService playbackService;
    private boolean serviceBound = false;
    private PlaybackState lastState = PlaybackState.EMPTY;
    
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicPlaybackService.MusicBinder binder = (MusicPlaybackService.MusicBinder) service;
            playbackService = binder.getService();
            playbackService.setListener(MainActivity.this);
            serviceBound = true;
            
            onStateUpdated(playbackService.getState());
            initVisualizer();
        }
        
        @Override
        public void onServiceDisconnected(ComponentName name) {
            playbackService = null;
            serviceBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            binding = ActivityMainBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            linkViews();
            setupClickListeners();
            initView();
            
            requestNotificationPermission();
            
            // Bind service
            Intent serviceIntent = new Intent(this, MusicPlaybackService.class);
            startService(serviceIntent);
            bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

            onAskPermission();
            handleNotificationAction(getIntent());

        } catch (Exception e) {
            Log.e("MainActivity", "Error in onCreate", e);
            Toast.makeText(this, "Error initializing app: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }
    
    private void linkViews() {
        visualizerView = binding.myvisualizerview;
        circularVisualizer = binding.mycricularvisualizer;
        tvSongTitle = binding.songtitle;
        tvBPM = binding.bpm;
        backgroundBeat = binding.backgroundBeat;
        soundPlate = binding.soundplate;
        mCroller = binding.croller;
        if (mCroller instanceof CircularSeekBar) {
            ((CircularSeekBar) mCroller).setSeekBarChangeListener(new CircularSeekBar.OnSeekChangeListener() {
                @Override
                public void onProgressChange(CircularSeekBar view, int newProgress) {
                }
            });
        } else if (mCroller instanceof DotCircularProgressBar) {
            Log.d("MainActivity", "Using premium DotCircularProgressBar");
        }
        
        tvHeaderSongName = binding.tvHeaderSongName;
        tvHeaderArtistName = binding.tvHeaderArtistName;
        ivAlbumArt = binding.albumArt;

        if (binding.circleviewWrapper != null) {
            binding.circleviewWrapper.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (lastState != null && lastState.isPlaying() && circularVisualizer != null) {
                        circularVisualizer.toggleMode();
                        Log.d("MainActivity", "Visualizer mode toggled");
                    }
                }
            });
        }
        
        circleViewWrapper = binding.circleviewWrapper;
        if (binding.bottomNavWrapper != null) {
            btnBrowser = binding.bottomNavWrapper.findViewById(R.id.btn_browser);
            btnFlash = (AppCompatImageView) binding.bottomNavWrapper.findViewById(R.id.flash_light);
        }
        progressBar = binding.progressBarm;
        btnPlay = binding.btnPlayback;
        btnNext = binding.btnPlayNext;
        btnPrev = binding.btnPlayPrev;
    }
    
    private void setupClickListeners() {
        if (binding == null) return;
        
        if (btnNext != null) btnNext.setOnClickListener(v -> onClickNext());
        if (btnPrev != null) btnPrev.setOnClickListener(v -> onClickPrev());
        if (btnPlay != null) btnPlay.setOnClickListener(v -> onClickPlay());
        
        if (binding.bottomNavWrapper != null) {
            View btnFacebook = binding.bottomNavWrapper.findViewById(R.id.btn_facebook);
            if (btnFacebook != null) btnFacebook.setOnClickListener(v -> onClickFacebook());
            
            View btnAbout = binding.bottomNavWrapper.findViewById(R.id.btn_about);
            if (btnAbout != null) btnAbout.setOnClickListener(v -> onClickAbout());
            
            if (btnBrowser != null) btnBrowser.setOnClickListener(v -> onClickBrowser());
            if (btnFlash != null) btnFlash.setOnClickListener(v -> onClickFlashLight());
        }
        
        if (progressBar instanceof SeekBar) {
            ((SeekBar) progressBar).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    stopProgressUpdates();
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    if (serviceBound && playbackService != null && lastState.getDuration() > 0) {
                        int percent = seekBar.getProgress(); // Assuming max 100 or 1000
                        int max = seekBar.getMax();
                        long newPos = (lastState.getDuration() * percent) / max;
                        playbackService.seekTo(newPos);
                    }
                    startProgressUpdates();
                }
            });
        }
    }

    public void initView() {
        try {
            utils = new Utilities();
            songManager = new SongManager();
            flashLightManager = new FlashLightManager(this);
            beatDetector = new BeatDetector();
            progressBarHandler = new ProgressBarHandler();
            
            useFlash = false;
            
            if (flashLightManager != null && flashLightManager.hasFlash()) {
                btnFlash.setImageResource(R.drawable.ic_flash_light_off);
            } else {
                btnFlash.setImageResource(R.drawable.ic_flash_light_disable);
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Error in initView", e);
        }
    }

    private void startRotation() {
        if (rotationAnimator == null) {
            rotationAnimator = android.animation.ObjectAnimator.ofFloat(circleViewWrapper, "rotation", 0f, 360f);
            rotationAnimator.setDuration(12000);
            rotationAnimator.setRepeatCount(android.animation.ObjectAnimator.INFINITE);
            rotationAnimator.setInterpolator(new android.view.animation.LinearInterpolator());
        }
        if (circleViewWrapper != null) {
            if (rotationAnimator.isPaused()) {
                rotationAnimator.resume();
            } else if (!rotationAnimator.isStarted()) {
                rotationAnimator.start();
            }
        }
    }

    private void stopRotation() {
        if (rotationAnimator != null && rotationAnimator.isStarted()) {
            rotationAnimator.pause();
        }
    }
    
    // ============================================================================================
    // Service Interaction & State Listener
    // ============================================================================================

    @Override
    public void onStateUpdated(PlaybackState state) {
        this.lastState = state;
        
        if (state.isPlaying()) {
            startProgressUpdates();
            startRotation();
        } else {
            stopProgressUpdates();
            stopRotation();
        }

        if (state.getSong() != null) {
            if (tvHeaderSongName != null) tvHeaderSongName.setText(state.getSong().getName());
            if (tvHeaderArtistName != null) tvHeaderArtistName.setText(state.getSong().getArtist());
            
            if (ivAlbumArt != null) {
                if (state.getSong().getAlbumArt() != null) {
                    android.graphics.Bitmap art = BitmapFactory.decodeByteArray(
                        state.getSong().getAlbumArt(), 0, state.getSong().getAlbumArt().length);
                    ivAlbumArt.setImageBitmap(art);
                } else {
                    ivAlbumArt.setImageResource(R.drawable.soundplate);
                }
            }
        }
        
        if (state.isPlaying()) {
            changePlayBtn(PlayState.PLAY);
            circularVisualizer.setActive(true);
            animRotate();
            if (mVisualizer != null) mVisualizer.setEnabled(true);
            
            startProgressUpdates();
        } else {
            changePlayBtn(PlayState.PAUSE);
            circularVisualizer.setActive(false);
            resetanim();
            if (mVisualizer != null) mVisualizer.setEnabled(false);
            
            stopProgressUpdates();
            turnOffFlash();
        }
        
        updateProgressUI(state.getCurrentPosition(System.currentTimeMillis()), state.getDuration());
        
        if (serviceBound && playbackService != null) {
            int currentSessionId = playbackService.getAudioSessionId();
            boolean sessionChanged = (currentSessionId != lastAudioSessionId);
            
            if ((mVisualizer == null || sessionChanged) && state.isPlaying()) {
                if (sessionChanged) {
                    if (mVisualizer != null) {
                        mVisualizer.release();
                        mVisualizer = null;
                    }
                }
                 initVisualizer();
                 lastAudioSessionId = currentSessionId;
            }
        }
    }
    
    // ============================================================================================
    // Progress Loop (Clock-based)
    // ============================================================================================
    
    private void startProgressUpdates() {
        progressBarHandler.start();
    }
    
    private void stopProgressUpdates() {
        progressBarHandler.stop();
    }
    
    private class ProgressBarHandler implements Runnable {
        private boolean isRunning = false;
        
        public void start() {
            if (!isRunning) {
                isRunning = true;
                mHandler.post(this);
            }
        }
        
        public void stop() {
            isRunning = false;
            mHandler.removeCallbacks(this);
        }

        @Override
        public void run() {
            if (!isRunning || !serviceBound || playbackService == null) return;
            
            PlaybackState state = playbackService.getState();
            if (state.isPlaying()) {
                long currentPos = state.getCurrentPosition(System.currentTimeMillis());
                long duration = state.getDuration();
                updateProgressUI(currentPos, duration);
                mHandler.postDelayed(this, 1000);
            } else {
                isRunning = false;
            }
        }
    }
    
    private void updateProgressUI(long current, long duration) {
        if (progressBar instanceof android.widget.ProgressBar && duration > 0) {
            int progress = (int) ((current * 100) / duration);
            android.widget.ProgressBar pb = (android.widget.ProgressBar) progressBar;
            if (pb.getMax() > 100) {
                progress = (int) ((current * pb.getMax()) / duration);
            }
            pb.setProgress(progress);
        }
        
        if (mCroller != null && duration > 0) {
            if (mCroller instanceof DotCircularProgressBar) {
                DotCircularProgressBar dcb = (DotCircularProgressBar) mCroller;
                int progress = (int) ((current * dcb.getMaxProgress()) / duration);
                dcb.setProgress(progress);
            } else if (mCroller instanceof CircularSeekBar) {
                CircularSeekBar csb = (CircularSeekBar) mCroller;
                int progress = (int) ((current * csb.getMaxProgress()) / duration);
                csb.setProgress(progress);
            } else if (mCroller instanceof android.widget.ProgressBar) {
                android.widget.ProgressBar pb = (android.widget.ProgressBar) mCroller;
                int progress = (int) ((current * pb.getMax()) / duration);
                pb.setProgress(progress);
            }
        }
        
        if (lastState != null && lastState.getSong() != null) {
            tvSongTitle.setText(utils.milliSecondsToTimer(current));
            tvBPM.setText(lastState.getSong().getBitrate() + " KBPS");
        }
    }

    // ============================================================================================
    // Controls
    // ============================================================================================

    public void onClickNext() {
        if (serviceBound && playbackService != null) {
            playbackService.skipToNext();
        }
    }

    public void onClickPrev() {
        if (serviceBound && playbackService != null) {
            playbackService.skipToPrevious();
        }
    }

    public void onClickPlay() {
        if (!serviceBound || playbackService == null) return;
        
        PlaybackState state = playbackService.getState();
        if (state.getSong() == null) {
            Toast.makeText(this, "Please select a song first", Toast.LENGTH_SHORT).show();
            onClickBrowser();
            return;
        }
        
        if (state.isPlaying()) {
            playbackService.pause();
        } else {
            playbackService.play();
        }
    }
    
    @Override
    public void onSongSelected(Song song) {
        if (songsList != null && serviceBound && playbackService != null) {
            int index = songsList.indexOf(song);
            if (index != -1) {
                playbackService.playSong(songsList, index);
            }
        }
        if (songListDialog != null && songListDialog.isShowing()) {
            songListDialog.dismiss();
        }
    }
    
    // ============================================================================================
    // Other Setup
    // ============================================================================================

    @AfterPermissionGranted(REQUEST_CAMERA_MIC_STORAGE)
    void onAskPermission() {
        String[] requiredPerms = getRequiredPermissions();
        if (EasyPermissions.hasPermissions(this, requiredPerms)) {
            // Permission granted
        } else {
            EasyPermissions.requestPermissions(this,
                    getString(R.string.request_permission_camera_record_storage),
                    REQUEST_CAMERA_MIC_STORAGE, requiredPerms);
        }
    }

    private void initVisualizer() {
        if (!serviceBound || playbackService == null) return;
        
        int sessionId = playbackService.getAudioSessionId();
        if (sessionId == 0) return;
        
        try {
            if (mVisualizer != null) {
                mVisualizer.release();
            }
            
            mVisualizer = new Visualizer(sessionId);
            int captureSize = Visualizer.getCaptureSizeRange()[1];
            mVisualizer.setCaptureSize(captureSize);
            mVisualizer.setDataCaptureListener(this, Visualizer.getMaxCaptureRate() / 2, true, true);
            mVisualizer.setEnabled(true);
            
            if (beatDetector != null) beatDetector.reset();
            
        } catch (Exception e) {
            Log.e("MainActivity", "Error initializing visualizer", e);
        }
    }

    // ... (Keep existing visualizer callbacks, anim methods, flash methods)

    // Browser Click
    public void onClickBrowser() {
        String[] requiredPerms = getRequiredPermissions();
        if (!EasyPermissions.hasPermissions(this, requiredPerms)) {
            EasyPermissions.requestPermissions(this,
                    getString(R.string.request_permission_camera_record_storage),
                    REQUEST_CAMERA_MIC_STORAGE, requiredPerms);
            return;
        }
        
        if (songsList == null || songsList.isEmpty()) {
            loadPlaylist();
        } else {
             showSongList();
        }
    }
    
    private void showSongList() {
        if (songListDialog != null) {
            songListDialog.dismiss();
        }
        songListDialog = new SongListDialog(MainActivity.this, songsList,
                android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        songListDialog.show();
    }
    
    private void loadPlaylist() {
        if (songManager == null) songManager = new SongManager(MainActivity.this);
        if (songsList == null) songsList = new ArrayList<>();
        
        if (songManager.hasCache()) {
            if (songsList.isEmpty()) {
                ArrayList<Song> cachedSongs = songManager.getPlayList();
                if (cachedSongs != null && !cachedSongs.isEmpty()) {
                    songsList.addAll(cachedSongs);
                    Log.d("MainActivity", "Loaded " + songsList.size() + " songs from cache instantly");
                }
            }
            showSongList();
        } else {
            // No cache - scan in background with progressive updates
            Log.d("MainActivity", "No cache found, starting progressive scan...");
            
            showSongList();
            
            new Thread(() -> {
                songManager.getPlayList(true, new SongManager.SongScanCallback() {
                    @Override
                    public void onSongFound(Song song) {
                        runOnUiThread(() -> {
                            // Add to local list and verify it's not a duplicate
                            if (!songsList.contains(song)) {
                                songsList.add(song);
                                // Update dialog if showing
                                if (songListDialog != null && songListDialog.isShowing()) {
                                    songListDialog.notifyDataSetChanged();
                                }
                            }
                        });
                    }
                    
                    @Override
                    public void onScanComplete(int totalSongs) {
                         runOnUiThread(() -> {
                             Log.d("MainActivity", "Scan complete. Total songs: " + totalSongs);
                             if (songListDialog != null && songListDialog.isShowing()) {
                                 songListDialog.notifyDataSetChanged();
                             }
                         });
                    }
                });
            }).start();
        }
    }


    public void onClickFacebook() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setData(Uri.parse("http://facebook.com/jquery404/"));
        startActivity(intent);
    }

    public void onClickAbout() {
        AboutDialog aboutDialog = new AboutDialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        aboutDialog.show();
    }
    
    public void onClickFlashLight() {
        Log.d("MainActivity", "Flash button clicked. Current useFlash: " + useFlash);
        if (flashLightManager.hasFlash()) {
            useFlash = !useFlash;
            Log.d("MainActivity", "Toggled useFlash to: " + useFlash);
            if (useFlash) {
                btnFlash.setImageResource(R.drawable.ic_flash_light);
            } else {
                btnFlash.setImageResource(R.drawable.ic_flash_light_off);
                flashLightManager.turnOffFlash();
            }
        } else {
            btnFlash.setImageResource(R.drawable.ic_flash_light_disable);
            Toast.makeText(this, "Your phone does not have the flash!", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void turnOffFlash() {
        if (flashLightManager != null) flashLightManager.turnOffFlash();
        if (btnFlash != null) btnFlash.setImageResource(R.drawable.ic_flash_light_off);
    }
    

    public void animShakeBox() {
        Animation shakeAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_shake);
        soundPlate.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        soundPlate.startAnimation(shakeAnimation);
    }

    public void animRotate() {
        Animation rotateAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_rotate);
        circleViewWrapper.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        circleViewWrapper.startAnimation(rotateAnimation);
    }
    
    private void resetanim() {
        circleViewWrapper.clearAnimation();
    }

    @Override
    public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
        if (circularVisualizer != null) {
            circularVisualizer.updateVisualizer(waveform);
        } else {
            Log.e("MainActivity", "circularVisualizer is NULL in receiving waveform");
        }
        if (visualizerView != null) {
            visualizerView.updateVisualizer(waveform);
        }
    }

    @Override
    public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
        if (beatDetector != null && beatDetector.detectBeat(fft)) {
            Log.d("MainActivity", "Beat detected! useFlash=" + useFlash + ", flashLightManager=" + (flashLightManager != null));
            animShakeBox();
            if (useFlash && flashLightManager != null) {
                Log.d("MainActivity", "Triggering flash");
                flashLightManager.flash();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            if (playbackService != null) playbackService.setListener(null);
            unbindService(serviceConnection);
            serviceBound = false;
        }
        stopProgressUpdates();
        if (mVisualizer != null) {
            mVisualizer.release();
            mVisualizer = null;
        }
        if (flashLightManager != null) {
            flashLightManager.release();
        }
    }
    
    // Helper
    public void changePlayBtn(PlayState state) {
        if (state == PlayState.DISABLE)
            btnPlay.setImageResource(R.drawable.ic_play_disable);
        else if (state == PlayState.PAUSE)
            btnPlay.setImageResource(R.drawable.ic_play);
        else if (state == PlayState.PLAY)
            btnPlay.setImageResource(R.drawable.ic_pause);
    }
    
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATION_PERMISSION);
            }
        }
    }
    
    private void handleNotificationAction(Intent intent) {
        if (intent == null) return;
        
        String action = intent.getAction();
        if (action == null) return;
        
        if (MediaNotificationReceiver.ACTION_PLAY.equals(action)) {
            onClickPlay();
        } else if (MediaNotificationReceiver.ACTION_PAUSE.equals(action)) {
            onClickPlay();
        } else if (MediaNotificationReceiver.ACTION_NEXT.equals(action)) {
            onClickNext();
        } else if (MediaNotificationReceiver.ACTION_PREV.equals(action)) {
            onClickPrev();
        }
    }
    
    // Method from interface EasyPermissions
    @Override
    public void onPermissionsGranted(int requestCode, java.util.List<String> perms) {}

    @Override
    public void onPermissionsDenied(int requestCode, java.util.List<String> perms) {}
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }
}
