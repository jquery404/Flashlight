package com.jquery404.flashlight.main;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatImageView;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.jquery404.flashlight.R;
import com.jquery404.flashlight.adapter.Song;
import com.jquery404.flashlight.custom.CircularSeekBar;
import com.jquery404.flashlight.manager.Utilities;

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
        Visualizer.OnDataCaptureListener, SeekBar.OnSeekBarChangeListener, CircularSeekBar.OnSeekChangeListener, EasyPermissions.PermissionCallbacks {

    @BindView(R.id.myvisualizerview)
    VisualizerView visualizerView;

    @BindView(R.id.bpm)
    TextView tvBPM;

    @BindView(R.id.seek_bar)
    SeekBar seekbar;
    @BindView(R.id.circular_seekbar)
    CircularSeekBar progressbar;

    @BindView(R.id.preview)
    SurfaceView preview;

    @BindView(R.id.background_beat)
    View backgroundBeat;
    @BindView(R.id.soundplate)
    View soundPlate;
    @BindView(R.id.circleview)
    View circleView;
    @BindView(R.id.circleview_wrapper)
    View circleViewWrapper;
    @BindView(R.id.btn_browser)
    View btnBrowser;

    @BindView(R.id.flash_light)
    AppCompatImageView btnFlash;
    @BindView(R.id.btn_playback)
    AppCompatImageView btnPlay;

    @BindView(R.id.adView)
    AdView adView;

    private static final int REQUEST_PATH = 121;
    private static final int REQUEST_CAMERA_MIC_STORAGE = 122;
    private String[] perms = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE};

    private enum PlayState {
        PLAY, PAUSE, STOP, DISABLE
    }

    private boolean allowStorage, allowMic, allowCamera;
    private MediaPlayer mMediaPlayer;
    private Visualizer mVisualizer;
    public float[] intensity = new float[4];
    private boolean isSongSelected, isSongPlaying;
    private Handler mHandler = new Handler();
    private Utilities utils;
    private Camera camera;
    private boolean useFlash;
    private boolean isFlashOn;
    private boolean hasFlash;
    Camera.Parameters params;
    SurfaceHolder mHolder;
    private int currentSongPos;
    private ArrayList<Song> songsList;

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

        seekbar.setOnSeekBarChangeListener(this);
        progressbar.setSeekBarChangeListener(this);

        seekbar.setPadding(0, 0, 0, 0);
        progressbar.invalidate();
        progressbar.hideSeekBar();
        useFlash = true;
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
            if (currentSongPos < (songsList.size() - 1)) {
                playNewSong(songsList.get(currentSongPos + 1));
                currentSongPos = currentSongPos + 1;
            } else {
                // play first song
                playNewSong(songsList.get(0));
                currentSongPos = 0;
            }
        }
    }

    @OnClick(R.id.btn_play_prev)
    public void onClickPrev() {
        if (isSongSelected) {
            if (currentSongPos > 0) {
                playNewSong(songsList.get(currentSongPos - 1));
                currentSongPos = currentSongPos - 1;
            } else {
                // play last song
                playNewSong(songsList.get(songsList.size() - 1));
                currentSongPos = songsList.size() - 1;
            }
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
        AboutActivity.start(this);
    }

    @OnClick(R.id.btn_playback)
    public void onClickPlay() {
        if (isSongSelected) {
            if (mMediaPlayer.isPlaying()) {
                pauseSong();
            } else if (!mMediaPlayer.isPlaying()) {
                playSong();
            }
        } else {
            Toast.makeText(this, "please select song first", Toast.LENGTH_SHORT).show();
            btnPlay.setImageResource(R.drawable.ic_play_disable);
        }
    }

    @OnClick(R.id.btn_browser)
    public void onClickBrowser() {
        Intent i = new Intent(this, PlayListActivity.class);
        startActivityForResult(i, REQUEST_PATH);
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
            android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(MainActivity.this);
            alert.setTitle("Error!");
            alert.setMessage("Your phone does not have the flash!");
            alert.setPositiveButton("OK", (d, i) -> finish());
        }
    }

    public void playNewSong(Song song) {
        try {
            if (mMediaPlayer == null)
                initAudio();
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(song.getPath());
            mMediaPlayer.prepare();
            mMediaPlayer.start();
            animRotate();

            initVisualizer();
            tvBPM.setText(song.getBitrate());

            progressbar.setProgress(0);
            progressbar.setMaxProgress(100);
            seekbar.setProgress(0);
            seekbar.setMax(100);
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

    public void playSong() {
        mMediaPlayer.start();
        changePlayBtn(PlayState.PLAY);
        animRotate();
    }

    public void pauseSong() {
        mMediaPlayer.pause();
        changePlayBtn(PlayState.PAUSE);
        resetanim();
    }


    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }


    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            long totalDuration = mMediaPlayer.getDuration();
            long currentDuration = mMediaPlayer.getCurrentPosition();

            int progress = (int) (utils.getProgressPercentage(currentDuration, totalDuration));
            seekbar.setProgress(progress);
            progressbar.setProgress(progress);
            progressbar.invalidate();

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
        if (isSongPlaying && !isSongSelected && mVisualizer != null) {
            mVisualizer.setEnabled(true);
            animRotate();
        }

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


    public static void start(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("flag", context.getClass().getSimpleName());
        context.startActivity(intent);
    }


    @Override
    public void onCompletion(MediaPlayer mp) {

        if (currentSongPos < (songsList.size() - 1)) {
            playNewSong(songsList.get(currentSongPos + 1));
            currentSongPos = currentSongPos + 1;

        } else {
            // play first song
            playNewSong(songsList.get(0));
            currentSongPos = 0;

        }
    }


    @Override
    public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
        visualizerView.updateVisualizer(waveform);

        if (waveform != null) {
            intensity[0] = ((float) waveform[0] + 128f) / 256;
            intensity[1] = ((float) waveform[1] + 128f) / 256;
            intensity[2] = ((float) waveform[2] + 128f) / 256;
            intensity[3] = ((float) waveform[3] + 128f) / 256;

            if (intensity[3] < 0.5f) {
                backgroundBeat.setBackgroundResource(R.drawable.background);
                if (useFlash)
                    turnOnFlash();
            } else {
                backgroundBeat.setBackgroundColor(utils.getColorId(getApplicationContext()));
                if (useFlash)
                    turnOffFlash();
            }
        }
    }

    @Override
    public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
        visualizerView.updateVisualizerFFT(fft);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
        int totalDuration = mMediaPlayer.getDuration();
        int currentPosition = utils.progressToTimer(seekBar.getProgress(), totalDuration);
        mMediaPlayer.seekTo(currentPosition);
        updateProgressBar();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            Toast.makeText(this, R.string.returned_from_app_settings_to_activity, Toast.LENGTH_SHORT)
                    .show();
        }

        if (requestCode == REQUEST_PATH) {
            if (resultCode == Activity.RESULT_OK) {
                Song song = new Song();
                song.setPath(data.getStringExtra("songPath"));
                song.setBitrate(data.getStringExtra("songBPM"));
                currentSongPos = data.getIntExtra("songPosition", 0);

                initCamera();

                if (songsList == null)
                    songsList = utils.getPlayList();

                playNewSong(song);
                isSongSelected = true;
            } else {
                isSongSelected = false;
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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

            params = camera.getParameters();
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

            params = camera.getParameters();
            params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            camera.setParameters(params);
            camera.stopPreview();
        }
    }

    @Override
    public void onProgressChange(CircularSeekBar view, int newProgress) {

    }
}
