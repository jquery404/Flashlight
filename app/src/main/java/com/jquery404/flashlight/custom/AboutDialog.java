package com.jquery404.flashlight.custom;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;

import com.jquery404.flashlight.R;

import com.jquery404.flashlight.databinding.ActivityAboutBinding;

/**
 * Created by faisal on 01/11/2017.
 */

public class AboutDialog extends Dialog implements DialogInterface.OnClickListener {
    private ActivityAboutBinding binding;

    public AboutDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnClose.setOnClickListener(v -> dismiss());
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
    }
}
