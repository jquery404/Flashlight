package com.jquery404.flashlight.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.jquery404.flashlight.R;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Faisal on 6/30/17.
 */

public class MainActivity extends BaseCompatActivity implements SurfaceHolder.Callback,
        MediaPlayer.OnCompletionListener, Visualizer.OnDataCaptureListener {

    /*

    @BindView(R.id.bit_a)
    View bitA;
    @BindView(R.id.songTitleLabel)
    TextView tvSongTitle;*/

    @BindView(R.id.myvisualizerview)
    VisualizerView visualizerView;
    @BindView(R.id.background_beat)
    View backgroundBeat;
    @BindView(R.id.soundplate)
    View soundPlate;
    @BindView(R.id.circleview)
    View circleView;
    @BindView(R.id.btn_browser)
    ImageButton btnBrowser;
    @BindView(R.id.adView)
    AdView adView;

    private static final int REQUEST_PATH = 1;
    private MediaPlayer mMediaPlayer;
    private Visualizer mVisualizer;
    public float[] intensity = new float[4];


    /*


    private Camera camera;
    private boolean isFlashOn;
    private boolean hasFlash;
    Camera.Parameters params;
    SurfaceHolder mHolder;
    SurfaceView preview;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_copy);

        ButterKnife.bind(this);

        initView();
        //initAudio();
        initEmu();

        /*checkFlash();
        isFlashOn = false;

        getCamera();
        initAudio();*/

    }


    public void initView() {
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    private void initEmu() {
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        mMediaPlayer = MediaPlayer.create(this, R.raw.attention);
        mMediaPlayer.setOnCompletionListener(this);
        setupVisualizer();
        mMediaPlayer.start();
    }

    private void initAudio() {
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnCompletionListener(this);
    }

    public void shakeBox() {
        Animation shakeAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_shake);
        soundPlate.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        soundPlate.startAnimation(shakeAnimation);
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    public void surfaceCreated(SurfaceHolder holder) {
        /*mHolder = holder;
        try {
            camera.setPreviewDisplay(mHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        /*camera.stopPreview();
        mHolder = null;*/
    }


    @OnClick(R.id.btn_browser)
    public void onClickBrowser() {
        Intent i = new Intent(this, PlayListActivity.class);
        startActivityForResult(i, REQUEST_PATH);
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == REQUEST_PATH) {
            if (resultCode == Activity.RESULT_OK) {
                String path = data.getStringExtra("songPath");
                playSong(path);
            }
        }

    }

    public void playSong(String path) {
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(path);
            mMediaPlayer.prepare();
            setupVisualizer();
            mMediaPlayer.setOnCompletionListener((m) -> mVisualizer.setEnabled(false));
            mMediaPlayer.start();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // on pause turn off the flash
        //turnOffFlash();

        if (isFinishing() && mMediaPlayer != null) {
            mVisualizer.release();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // on stop release the camera
        /*if (camera != null) {
            camera.release();
            camera = null;
        }*/
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
        mVisualizer.setEnabled(false);
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
                backgroundBeat.setBackgroundColor(ContextCompat.getColor(
                        getApplicationContext(), R.color.bit7));
                //turnOnFlash();
                shakeBox();
            } else {
                backgroundBeat.setBackgroundColor(ContextCompat.getColor(
                        getApplicationContext(), R.color.bit1));
                //turnOffFlash();
            }
        }
    }

    @Override
    public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
        visualizerView.updateVisualizerFFT(fft);
    }






    /*public void checkFlash() {
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







    @Override
    protected void onStart() {
        super.onStart();
        getCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    */



    /*@OnClick(R.id.toggle_btn_play)
    public void onTogglePlay() {
        Intent i = new Intent(getApplicationContext(), PlayListActivity.class);
        startActivityForResult(i, 100);
    }

    */


    /*private void turnOnFlash() {
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
    }*/

}
