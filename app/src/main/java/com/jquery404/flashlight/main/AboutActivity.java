package com.jquery404.flashlight.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.jquery404.flashlight.R;

import com.jquery404.flashlight.databinding.ActivityAboutBinding;

/**
 * Created by Faisal on 6/30/17.
 */

public class AboutActivity extends BaseCompatActivity {
    private ActivityAboutBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initViews();
    }

    private void initViews() {
        binding.appVersion.setText(getString(R.string.app_name) + " 2.0.0");
        binding.btnClose.setOnClickListener(v -> finish());
    }

    public static void start(Context context) {
        Intent launchIntent = new Intent(context, AboutActivity.class);
        launchIntent.putExtra("flag", context.getClass().getSimpleName());
        context.startActivity(launchIntent);
    }
}
