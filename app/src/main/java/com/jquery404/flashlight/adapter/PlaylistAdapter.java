package com.jquery404.flashlight.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AlertDialogLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jquery404.flashlight.R;
import com.jquery404.flashlight.custom.OnSongSelectedListener;
import com.jquery404.flashlight.main.MainActivity;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Faisal on 7/26/17.
 */

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistHolder> {
    private ArrayList<Song> songs = new ArrayList<>();
    private Context context;
    private Dialog dialog;
    private OnSongSelectedListener listener;

    public PlaylistAdapter(Context context, ArrayList<Song> songs,
                           OnSongSelectedListener listener, Dialog dialog) {
        this.context = context;
        this.songs = songs;
        this.listener = listener;
        this.dialog = dialog;
    }

    @Override
    public PlaylistHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.playlist_item, viewGroup, false);
        return new PlaylistHolder(v, context, songs, listener, dialog);
    }

    @Override
    public void onBindViewHolder(PlaylistHolder holder, int i) {
        Song song = songs.get(i);
        holder.songTitle.setText(song.getName());
        holder.songDuration.setText(song.getDuration());
        holder.songArtist.setText(song.getArtist());
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }


    class PlaylistHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.song_title)
        TextView songTitle;
        @BindView(R.id.song_duration)
        TextView songDuration;
        @BindView(R.id.song_artist)
        TextView songArtist;

        ArrayList<Song> songs = new ArrayList<>();
        Context context;
        Dialog dialog;
        OnSongSelectedListener listener;

        PlaylistHolder(View itemView, Context context, ArrayList<Song> songs,
                       OnSongSelectedListener listener, Dialog dialog) {
            super(itemView);
            this.context = context;
            this.songs = songs;
            this.listener = listener;
            this.dialog = dialog;

            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.card_view)
        void onClick() {
            Song song = songs.get(getAdapterPosition());
            listener.onSongSelected(song);
            dialog.dismiss();

        }
    }
}
