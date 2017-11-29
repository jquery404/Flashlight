package com.jquery404.flashlight.custom;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;

import com.jquery404.flashlight.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by faisal on 01/11/2017.
 */

public class AboutDialog extends Dialog implements DialogInterface.OnClickListener {


    public AboutDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        ButterKnife.bind(this);
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {

    }

    @OnClick(R.id.btn_close)
    public void onClickClose() {
        dismiss();
    }
}
