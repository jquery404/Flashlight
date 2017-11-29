package com.jquery404.flashlight.main;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatImageView;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.jquery404.flashlight.R;
import com.jquery404.flashlight.adapter.Song;
import com.jquery404.flashlight.custom.AboutDialog;
import com.jquery404.flashlight.custom.OnSongSelectedListener;
import com.jquery404.flashlight.custom.SongListDialog;
import com.jquery404.flashlight.custom.SongManager;
import com.jquery404.flashlight.manager.Utilities;
import com.sdsmdg.harjot.crollerTest.Croller;
import com.sdsmdg.harjot.crollerTest.OnCrollerChangeListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by Faisal on 6/30/17.
 */

public class MainActivity extends BaseCompatActivity implements SurfaceHolder.Callback, MediaPlayer.OnCompletionListener,
        Visualizer.OnDataCaptureListener, OnCrollerChangeListener, OnSongSelectedListener, EasyPermissions.PermissionCallbacks {

    @BindView(R.id.myvisualizerview)
    VisualizerView visualizerView;

    @BindView(R.id.mycricularvisualizer)
    CircularVisualizerView circularVisualizer;

    @BindView(R.id.songtitle)
    TextView tvSongTitle;
    @BindView(R.id.bpm)
    TextView tvBPM;

    @BindView(R.id.preview)
    SurfaceView preview;

    @BindView(R.id.background_beat)
    View backgroundBeat;
    @BindView(R.id.soundplate)
    View soundPlate;

    @BindView(R.id.croller)
    Croller mCroller;

    @BindView(R.id.circleview_wrapper)
    View circleViewWrapper;
    @BindView(R.id.btn_browser)
    View btnBrowser;
    @BindView(R.id.progressBarm)
    View progressBar;

    @BindView(R.id.flash_light)
    AppCompatImageView btnFlash;
    @BindView(R.id.btn_playback)
    AppCompatImageView btnPlay;
    @BindView(R.id.btn_play_next)
    AppCompatImageView btnNext;
    @BindView(R.id.btn_play_prev)
    AppCompatImageView btnPrev;

    @BindView(R.id.adView)
    AdView adView;

    private static final int REQUEST_PATH = 121;
    private static final int REQUEST_CAMERA_MIC_STORAGE = 122;
    private boolean cameraInitialize = false;
    private String[] perms = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE};

    private enum PlayState {
        PLAY, PAUSE, STOP, DISABLE
    }

    private InterstitialAd mInterstitialAd;
    private boolean allowStorage, allowMic, allowCamera;
    private MediaPlayer mMediaPlayer;
    private Visualizer mVisualizer;
    public float[] intensity = new float[4];
    private boolean isSongSelected, isSongPlaying;
    private Handler mHandler = new Handler();
    private Utilities utils;
    private SongManager songManager;
    private Camera camera;
    private boolean useFlash;
    private boolean isFlashOn;
    private boolean hasFlash;
    Camera.Parameters params;
    SurfaceHolder mHolder;
    private ArrayList<Song> songsList;
    private SongListDialog songListDialog;
    boolean doubleBackToExitPressedOnce = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        initView();

        onAskPermission();
    }


    public void initView() {
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        utils = new Utilities();
        songManager = new SongManager();

        mCroller.setLabel("");

        useFlash = true;
    }

    public void resumeSong() {
        circularVisualizer.setActive(true);

        mMediaPlayer.start();
        changePlayBtn(MainActivity.PlayState.PLAY);
        animRotate();
    }

    public void pauseSong() {
        circularVisualizer.setActive(false);
        mMediaPlayer.pause();
        changePlayBtn(MainActivity.PlayState.PAUSE);
        resetanim();
    }

    @AfterPermissionGranted(REQUEST_CAMERA_MIC_STORAGE)
    void onAskPermission() {
        if (EasyPermissions.hasPermissions(this, perms)) {
            initAudio();
        } else {
            EasyPermissions.requestPermissions(this,
                    getString(R.string.request_permission_camera_record_storage),
                    REQUEST_CAMERA_MIC_STORAGE, perms);
        }
    }


    private void initAudio() {
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnCompletionListener(this);
    }

    private void initCamera() {
        if (checkFlash()) {
            hasFlash = true;
            getCamera();

        } else {
            hasFlash = false;
            btnFlash.setImageResource(R.drawable.ic_flash_light_disable);
            android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(MainActivity.this);
            alert.setTitle("Error!");
            alert.setMessage("Your phone does not have the flash!");
            alert.setPositiveButton("OK", (d, i) -> finish());
        }
    }

    private void initVisualizer() {
        if (mVisualizer == null) {
            mVisualizer = new Visualizer(mMediaPlayer.getAudioSessionId());
            mVisualizer.setDataCaptureListener(this, Visualizer.getMaxCaptureRate() / 2, true, false);
        }
        mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
        mVisualizer.setEnabled(true);
    }

    private void initInterstitialAd() {
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_full_screen));
        AdRequest intAdRequest = new AdRequest.Builder().build();
        mInterstitialAd.loadAd(intAdRequest);
        mInterstitialAd.setAdListener(new AdListener() {
            public void onAdLoaded() {
                showInterstitial();
            }
        });
    }

    private void showInterstitial() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
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

    // getting camera parameters
    private void getCamera() {
        mHolder = preview.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        if (camera == null) {
            try {
                camera = Camera.open();
                params = camera.getParameters();
                try {
                    camera.setPreviewDisplay(mHolder);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (RuntimeException e) {
                Log.e("SUCA.", e.getMessage());
            }
        }
    }


    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    public void surfaceCreated(SurfaceHolder holder) {
        mHolder = holder;
        if (camera != null) {
            try {
                camera.setPreviewDisplay(mHolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        if (camera != null) {
            camera.stopPreview();
            mHolder = null;
        }
    }

    @OnClick(R.id.btn_play_next)
    public void onClickNext() {
        if (isSongSelected) {
            Song newSong = songManager.playNextSong();
            this.onSongSelected(newSong);
        }
    }

    @OnClick(R.id.btn_play_prev)
    public void onClickPrev() {
        if (isSongSelected) {
            Song newSong = songManager.playPrevSong();
            this.onSongSelected(newSong);
        }
    }

    @OnClick(R.id.btn_facebook)
    public void onClickFacebook() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setData(Uri.parse("http://facebook.com/jquery404/"));
        startActivity(intent);
    }

    @OnClick(R.id.btn_about)
    public void onClickAbout() {
        AboutDialog aboutDialog = new AboutDialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        aboutDialog.show();
        initInterstitialAd();
    }

    @OnClick(R.id.btn_playback)
    public void onClickPlay() {
        if (!isSongSelected && !isSongPlaying) {
            // TODO: 11/10/2017 change to alert dialog
            Toast.makeText(this, "please select song first", Toast.LENGTH_SHORT).show();
            btnPlay.setImageResource(R.drawable.ic_play_disable);
        } else {
            if (mMediaPlayer.isPlaying()) {
                pauseSong();
            } else if (!mMediaPlayer.isPlaying()) {
                resumeSong();
            }
        }
    }

    @OnClick(R.id.btn_browser)
    public void onClickBrowser() {
        if (songsList == null) {
            new ReadSongFile().execute();

        } else {

            /*songListDialog = new SongListDialog(this, songsList,
                    android.R.style.Theme_Black_NoTitleBar_Fullscreen);*/
            songListDialog.show();
        }
    }

    @OnClick(R.id.flash_light)
    public void onClickFlashLight() {
        if (hasFlash) {
            useFlash = !useFlash;
            if (useFlash) {
                btnFlash.setImageResource(R.drawable.ic_flash_light);
            } else {
                btnFlash.setImageResource(R.drawable.ic_flash_light_off);
                turnOffFlash();
            }
        } else {
            btnFlash.setImageResource(R.drawable.ic_flash_light_disable);
            Toast.makeText(this, "Your phone does not have the flash!", Toast.LENGTH_SHORT).show();
        }
    }

    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }


    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            long totalDuration = mMediaPlayer.getDuration();
            long currentDuration = mMediaPlayer.getCurrentPosition();

            int progress = (int) (utils.getProgressPercentage(currentDuration, totalDuration));
            mCroller.setProgress(progress);

            mHandler.postDelayed(this, 100);
        }
    };

    private void resetanim() {
        circleViewWrapper.clearAnimation();
    }


    @Override
    protected void onPause() {
        super.onPause();

        // on pause turn off the flash
        turnOffFlash();
        resetanim();

        if (mVisualizer != null)
            mVisualizer.setEnabled(false);

        if (isFinishing() && mMediaPlayer != null) {
            if (mVisualizer != null)
                mVisualizer.release();
            mHandler.removeCallbacks(mUpdateTimeTask);
            mMediaPlayer.release();
            mMediaPlayer = null;
        }


    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isSongPlaying && mVisualizer != null) {
            mVisualizer.setEnabled(true);
            animRotate();
        }

    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    @Override
    protected void onStop() {
        super.onStop();

        // on stop release the camera
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }


    public void changePlayBtn(PlayState state) {
        if (state == PlayState.DISABLE)
            btnPlay.setImageResource(R.drawable.ic_play_disable);
        else if (state == PlayState.PAUSE)
            btnPlay.setImageResource(R.drawable.ic_play);
        else if (state == PlayState.PLAY)
            btnPlay.setImageResource(R.drawable.ic_pause);
    }

    public void changeNextPrevBtn() {
        if (songsList.size() > 0) {
            btnNext.setImageResource(R.drawable.ic_play_next);
            btnPrev.setImageResource(R.drawable.ic_play_prev);
        }
    }


    public static void start(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("flag", context.getClass().getSimpleName());
        context.startActivity(intent);
    }


    @Override
    public void onCompletion(MediaPlayer mp) {
        Song nextSong = songManager.playNextSong();
        onSongSelected(nextSong);
        tvSongTitle.setText(nextSong.getName());
    }


    @Override
    public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
        visualizerView.updateVisualizer(waveform);
        circularVisualizer.updateVisualizer(waveform);

        if (waveform != null) {
            intensity[0] = ((float) waveform[0] + 128f) / 256;
            intensity[1] = ((float) waveform[1] + 128f) / 256;
            intensity[2] = ((float) waveform[2] + 128f) / 256;
            intensity[3] = ((float) waveform[3] + 128f) / 256;

            if (intensity[3] < 0.5f) {
                //backgroundBeat.setBackgroundResource(R.drawable.background);
                mCroller.setMainCircleColor(R.drawable.background);
                if (useFlash)
                    turnOnFlash();
            } else {
                //backgroundBeat.setBackgroundColor(utils.getColorId(getApplicationContext()));
                mCroller.setMainCircleColor(utils.getColorId(getApplicationContext()));
                if (useFlash)
                    turnOffFlash();
            }
        }
    }

    @Override
    public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
        visualizerView.updateVisualizerFFT(fft);
        circularVisualizer.updateVisualizer(fft);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, MainActivity.this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Log.d("TAG", "onPermissionsDenied:" + requestCode + ":" + perms.size());
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }


    public boolean checkFlash() {
        return getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    private void turnOnFlash() {
        if (!isFlashOn && hasFlash) {
            if (camera == null || params == null) {
                return;
            }

            isFlashOn = true;

            params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(params);
            camera.startPreview();
        }
    }

    private void turnOffFlash() {
        if (isFlashOn && hasFlash) {
            if (camera == null || params == null) {
                return;
            }

            isFlashOn = false;

            params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            camera.setParameters(params);
            camera.stopPreview();
        }
    }

    @Override
    public void onProgressChanged(Croller croller, int progress) {

    }

    @Override
    public void onStartTrackingTouch(Croller croller) {
        mHandler.removeCallbacks(mUpdateTimeTask);
    }

    @Override
    public void onStopTrackingTouch(Croller croller) {
        mHandler.removeCallbacks(mUpdateTimeTask);
        int totalDuration = mMediaPlayer.getDuration();
        int currentPosition = utils.progressToTimer(croller.getProgress(), totalDuration);
        mMediaPlayer.seekTo(currentPosition);
        updateProgressBar();
    }

    @Override
    public void onSongSelected(Song song) {

        if (!isSongSelected) {
            mCroller.setOnCrollerChangeListener(this);
            isSongSelected = true;
        }

        try {
            if (mMediaPlayer == null)
                initAudio();

            if (!cameraInitialize) {
                initCamera();
                cameraInitialize = true;
            }
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(song.getPath());
            mMediaPlayer.prepare();
            mMediaPlayer.start();
            animRotate();

            initVisualizer();
            tvBPM.setText(getString(R.string.bpm_title, song.getBitrate()));
            tvSongTitle.setText(song.getName());

            song.setNextSong(songManager.getNextSong(songManager.getCurrentSongPos()));

            changePlayBtn(MainActivity.PlayState.PLAY);
            changeNextPrevBtn();

            updateProgressBar();

            isSongPlaying = true;

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private class ReadSongFile extends AsyncTask<Void, Void, ArrayList<Song>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected ArrayList<Song> doInBackground(Void... params) {
            return songManager.getPlayList();
        }

        @Override
        protected void onPostExecute(ArrayList<Song> songs) {
            super.onPostExecute(songs);
            progressBar.setVisibility(View.GONE);
            songsList = songs;

            songListDialog = new SongListDialog(MainActivity.this, songsList,
                    android.R.style.Theme_Black_NoTitleBar_Fullscreen);
            songListDialog.show();

        }

    }


}
