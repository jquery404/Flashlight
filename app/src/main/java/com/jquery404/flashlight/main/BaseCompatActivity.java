package com.jquery404.flashlight.main;

import android.support.v7.app.AppCompatActivity;

/**
 * Created by Faisal on 6/30/17.
 */

public abstract class BaseCompatActivity extends AppCompatActivity {

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }
}
