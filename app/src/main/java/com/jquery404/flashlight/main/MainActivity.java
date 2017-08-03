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
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.jquery404.flashlight.R;
import com.jquery404.flashlight.adapter.Song;
import com.jquery404.flashlight.manager.Utilities;

import java.io.IOException;
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
        Visualizer.OnDataCaptureListener, SeekBar.OnSeekBarChangeListener, EasyPermissions.PermissionCallbacks {

    @BindView(R.id.myvisualizerview)
    VisualizerView visualizerView;

    @BindView(R.id.bpm)
    TextView tvBPM;

    @BindView(R.id.seek_bar)
    SeekBar progressbar;

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

    @BindView(R.id.adView)
    AdView adView;

    private static final int REQUEST_PATH = 121;
    private static final int REQUEST_CAMERA_MIC_STORAGE = 122;
    private String[] perms = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE};

    private MediaPlayer mMediaPlayer;
    private Visualizer mVisualizer;
    public float[] intensity = new float[4];
    public float colorCounter = 0;
    private boolean isSongSelected, isSongPlaying;
    private Handler mHandler = new Handler();
    private Utilities utils;
    private Camera camera;
    private boolean isFlashOn;
    private boolean hasFlash;
    Camera.Parameters params;
    SurfaceHolder mHolder;

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
        progressbar.setPadding(0, 0, 0, 0);
    }

    private void initEmu() {
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        mMediaPlayer = MediaPlayer.create(this, R.raw.attention);
        mMediaPlayer.setOnCompletionListener(this);
        progressbar.setOnSeekBarChangeListener(this);
        progressbar.setMax(mMediaPlayer.getDuration());

        setupVisualizer();
        mMediaPlayer.start();

        animRotate();
    }

    private void initAudio() {
        circleViewWrapper.clearAnimation();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        progressbar.setOnSeekBarChangeListener(this);
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnCompletionListener(this);
        utils = new Utilities();
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
        try {
            camera.setPreviewDisplay(mHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        if (camera != null) {
            camera.stopPreview();
            mHolder = null;
        }
    }


    @OnClick(R.id.btn_browser)
    public void onClickBrowser() {
        Intent i = new Intent(this, PlayListActivity.class);
        startActivityForResult(i, REQUEST_PATH);
    }

    public void playSong(Song song) {
        try {
            checkCamera();

            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(song.getPath());
            mMediaPlayer.prepare();
            setupVisualizer();
            mMediaPlayer.start();
            animRotate();

            tvBPM.setText(song.getBitrate());

            progressbar.setProgress(0);
            progressbar.setMax(100);
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

    private void checkCamera(){
        if (checkFlash()) {
            hasFlash = true;
            getCamera();
        } else {
            android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(MainActivity.this);
            alert.setTitle("Error!");
            alert.setMessage("Your phone does not have the flash!");
            alert.setPositiveButton("OK", (d, i) -> finish());
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
            progressbar.setProgress(progress);

            mHandler.postDelayed(this, 100);
        }
    };

    private void resetanim() {
        circleViewWrapper.clearAnimation();
        soundPlate.clearAnimation();
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

    private void setupVisualizer() {
        mVisualizer = new Visualizer(mMediaPlayer.getAudioSessionId());
        mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
        mVisualizer.setDataCaptureListener(this, Visualizer.getMaxCaptureRate() / 2, true, false);
        mVisualizer.setEnabled(true);
    }


    public static void start(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("flag", context.getClass().getSimpleName());
        context.startActivity(intent);
    }


    @Override
    public void onCompletion(MediaPlayer mp) {
        isSongPlaying = false;
        tvBPM.setText(R.string.default_bpm);
        mVisualizer.setEnabled(false);
        resetanim();
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
               // backgroundBeat.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.bit7));
                animShakeBox();
                turnOnFlash();
            } else {
                //backgroundBeat.setBackgroundColor(Utils.getColorId(getApplicationContext()));
                colorCounter += 0.03f;
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == REQUEST_PATH) {
            if (resultCode == Activity.RESULT_OK) {
                Song song = new Song();
                song.setPath(data.getStringExtra("songPath"));
                song.setBitrate(data.getStringExtra("songBPM"));
                playSong(song);
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
        Log.d("TAG", "Permission has been granted");
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Log.d("TAG", "Permission has been denied");
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

}
