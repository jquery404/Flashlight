package com.jquery404.flashlight;


import androidx.appcompat.app.AppCompatActivity;

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
