package com.jquery404.flashlight.custom;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import com.jquery404.flashlight.R;
import com.jquery404.flashlight.adapter.PlaylistAdapter;
import com.jquery404.flashlight.adapter.Song;

import java.util.ArrayList;

import com.jquery404.flashlight.databinding.LayoutSonglistBinding;

/**
 * Created by faisal on 11/10/2017.
 */

public class SongListDialog extends Dialog implements DialogInterface.OnClickListener {
    private LayoutSonglistBinding binding;
    private ArrayList<Song> songsList;
    private final OnSongSelectedListener listener;

    public SongListDialog(@NonNull Context context, ArrayList<Song> songs, @StyleRes int themeResId) {
        super(context, themeResId);
        this.songsList = songs;
        this.listener = (OnSongSelectedListener) context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = LayoutSonglistBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (songsList != null) {
            binding.recyclerPlaylist.setLayoutManager(new LinearLayoutManager(getContext()));
            binding.progressBar.setVisibility(View.VISIBLE);
            RecyclerView.Adapter adapter = new PlaylistAdapter(getContext(), songsList, listener, this);
            binding.recyclerPlaylist.setAdapter(adapter);
            binding.recyclerPlaylist.setNestedScrollingEnabled(false);
            binding.progressBar.setVisibility(View.GONE);
        }

        binding.btnClose.setOnClickListener(v -> dismiss());
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
    }
    
    public void notifyDataSetChanged() {
        if (binding != null && binding.recyclerPlaylist.getAdapter() != null) {
            binding.recyclerPlaylist.getAdapter().notifyDataSetChanged();
        }
    }
}
