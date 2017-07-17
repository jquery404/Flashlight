package com.jquery404.flashlight.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.jquery404.flashlight.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Faisal on 6/30/17.
 */

public class AboutActivity extends BaseCompatActivity {
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);

        initViews();
    }

    private void initViews() {

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_full_screen));
        AdRequest intAdRequest = new AdRequest.Builder().build();
        mInterstitialAd.loadAd(intAdRequest);
        mInterstitialAd.setAdListener(new AdListener() {
            public void onAdLoaded() {
                showInterstitial();
            }
        });
    }

    private void showInterstitial() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }

    public static void start(Context context) {
        Intent launchIntent = new Intent(context, AboutActivity.class);
        launchIntent.putExtra("flag", context.getClass().getSimpleName());
        context.startActivity(launchIntent);
    }

    @OnClick(R.id.btn_close)
    public void onClickClose() {
        finish();
    }
}
