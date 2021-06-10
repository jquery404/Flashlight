package com.jquery404.flashlight.custom;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jquery404.flashlight.R;
import com.jquery404.flashlight.adapter.PlaylistAdapter;
import com.jquery404.flashlight.adapter.Song;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by faisal on 11/10/2017.
 */

public class SongListDialog extends Dialog implements DialogInterface.OnClickListener {

    @BindView(R.id.recycler_playlist)
    RecyclerView recycler_playlist;

    @BindView(R.id.progressBar)
    View progressBar;


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
        setContentView(R.layout.layout_songlist);

        ButterKnife.bind(this);


        if (songsList != null) {
            recycler_playlist.setLayoutManager(new LinearLayoutManager(getContext()));
            progressBar.setVisibility(View.VISIBLE);
            RecyclerView.Adapter adapter = new PlaylistAdapter(getContext(), songsList, listener, this);
            recycler_playlist.setAdapter(adapter);
            recycler_playlist.setNestedScrollingEnabled(false);
            progressBar.setVisibility(View.GONE);
        }

    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {

    }


    @OnClick(R.id.btn_close)
    public void onClickClose() {
        dismiss();
    }
}
