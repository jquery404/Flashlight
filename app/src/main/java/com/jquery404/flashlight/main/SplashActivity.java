package com.jquery404.flashlight.main;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

import com.jquery404.flashlight.R;

/**
 * Created by Faisal on 6/30/17.
 */

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar_FullScreen);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(
                () -> {
                    loadCompassApp();
                }, 1000);
    }

    private void loadCompassApp() {
        MainActivity.start(this);
        finish();
    }
}
