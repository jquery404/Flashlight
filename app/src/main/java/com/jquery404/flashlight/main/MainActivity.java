package com.jquery404.flashlight.main;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.jquery404.flashlight.R;
import com.pddstudio.easyflashlight.EasyFlashlight;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Faisal on 6/30/17.
 */

public class MainActivity extends BaseCompatActivity {

    @BindView(R.id.myvisualizerview)
    VisualizerView visualizerView;

    @BindView(R.id.latlngHeading)
    TextView tvHeading;

    private MediaPlayer mMediaPlayer;
    private Visualizer mVisualizer;
    public float intensity = 0;
    private boolean toggleTorch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        boolean isFlashAvailable = getApplicationContext().getPackageManager().
                hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        if (!isFlashAvailable) {
            showMessage();
        } else {
            initTorch();
            initAudio();
        }
    }

    private void initTorch() {
        EasyFlashlight.init(this);
    }

    private void initAudio() {
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        mMediaPlayer = MediaPlayer.create(this, R.raw.attention);

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
                    intensity = ((float) waveform[0] + 128f) / 256;

                    if (intensity < 0.5f) {

                    } else {
                    }

                    Log.d("vis", String.valueOf(intensity));
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
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (isFinishing() && mMediaPlayer != null) {
            mVisualizer.release();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    public static void start(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("flag", context.getClass().getSimpleName());
        context.startActivity(intent);
    }

    private void showMessage() {
        AlertDialog alert = new AlertDialog.Builder(this).create();
        alert.setTitle("Error!!");
        alert.setMessage("Your device doesn't support flash light!");
        alert.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", (d, w) -> {
            finish();
            System.exit(0);
        });
    }

    @OnClick(R.id.btn_facebook)
    public void onToggleClick() {
        toggleTorch = !toggleTorch;

        if (toggleTorch) {
            Toast.makeText(this, "permission", Toast.LENGTH_SHORT).show();
            EasyFlashlight.getInstance().turnOn();
        } else {
            EasyFlashlight.getInstance().turnOff();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        EasyFlashlight.getInstance().onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}
