package com.jquery404.flashlight.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.jquery404.flashlight.BaseCompatActivity;
import com.jquery404.flashlight.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Faisal on 6/30/17.
 */

public class AboutActivity extends BaseCompatActivity {
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.app_version)
    TextView tvVersion;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);

        initViews();
    }

    private void initViews() {

    }

    private void showInterstitial() {

    }

    public static void start(Context context) {
        Intent launchIntent = new Intent(context, AboutActivity.class);
        launchIntent.putExtra("flag", context.getClass().getSimpleName());
        context.startActivity(launchIntent);
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.btn_close)
    public void onClickClose() {
        finish();
    }
}
