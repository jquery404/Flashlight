package com.jquery404.flashlight.main;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.os.Message;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

import com.jquery404.flashlight.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

/**
 * Created by Faisal on 6/30/17.
 */

public class MainActivity extends BaseCompatActivity implements SurfaceHolder.Callback {

    @BindView(R.id.myvisualizerview)
    VisualizerView visualizerView;

    @BindView(R.id.bit_a)
    View bitA;


    private MediaPlayer mMediaPlayer;
    private Visualizer mVisualizer;
    public float[] intensity = new float[4];
    private Camera camera;
    private boolean isFlashOn;
    private boolean hasFlash;
    Camera.Parameters params;
    SurfaceHolder mHolder;
    SurfaceView preview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        checkFlash();
        isFlashOn = false;

        getCamera();
        initAudio();
    }


    public void checkFlash() {
        hasFlash = getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        if (!hasFlash) {
            android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(MainActivity.this);
            alert.setTitle("Error!");
            alert.setMessage("Your phone does not have the flash!");
            alert.setPositiveButton("OK", (d, i) -> finish());
        } else {
            Toast.makeText(this, "Have", Toast.LENGTH_SHORT).show();
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
        camera.stopPreview();
        mHolder = null;
    }


    // getting camera parameters
    private void getCamera() {
        preview = (SurfaceView) findViewById(R.id.PREVIEW);
        mHolder = preview.getHolder();
        //mHolder.addCallback(this);
        mHolder.addCallback(MainActivity.this);
        //Android < 2.3.6 ha bisogno di sto hack
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


    private void initAudio() {
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        mMediaPlayer = MediaPlayer.create(this, R.raw.till_i_co);

        setupVisualizerFxAndUI();
        mVisualizer.setEnabled(true);
        mMediaPlayer.setOnCompletionListener((m) -> mVisualizer.setEnabled(false));
        mMediaPlayer.start();
    }


    private void setupVisualizerFxAndUI() {
        mVisualizer = new Visualizer(mMediaPlayer.getAudioSessionId());
        mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            mVisualizer.setScalingMode(Visualizer.SCALING_MODE_NORMALIZED);
        mVisualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {

            @Override
            public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
                visualizerView.updateVisualizer(waveform);
                if (waveform != null) {
                    intensity[0] = ((float) waveform[0] + 128f) / 256;
                    intensity[1] = ((float) waveform[1] + 128f) / 256;
                    intensity[2] = ((float) waveform[2] + 128f) / 256;
                    intensity[3] = ((float) waveform[3] + 128f) / 256;

                    if (intensity[3] < 0.5f) {
                        bitA.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.bit6));
                        turnOnFlash();
                    } else {
                        bitA.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.bit7));
                        turnOffFlash();
                    }

                }
            }

            @Override
            public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
                visualizerView.updateVisualizerFFT(fft);
            }
        }, Visualizer.getMaxCaptureRate() / 2, true, false);
    }


    @Override
    protected void onStart() {
        super.onStart();
        getCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // on pause turn off the flash
        turnOffFlash();

        if (isFinishing() && mMediaPlayer != null) {
            mVisualizer.release();
            mMediaPlayer.release();
            mMediaPlayer = null;
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

    public static void start(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("flag", context.getClass().getSimpleName());
        context.startActivity(intent);
    }


    @OnClick(R.id.btn_play)
    public void onTogglePlay() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        } else {
            mMediaPlayer.start();
        }
    }


    /*
    * Turning On flash
    */
    private void turnOnFlash() {
        if (!isFlashOn) {
            if (camera == null || params == null) {
                return;
            }

            isFlashOn = true;

            params = camera.getParameters();
            params.setFlashMode(Parameters.FLASH_MODE_TORCH);
            camera.setParameters(params);
            camera.startPreview();
        }
    }

    private void turnOffFlash() {
        if (isFlashOn) {
            if (camera == null || params == null) {
                return;
            }

            isFlashOn = false;

            params = camera.getParameters();
            params.setFlashMode(Parameters.FLASH_MODE_OFF);
            camera.setParameters(params);
            camera.stopPreview();
        }
    }



}
