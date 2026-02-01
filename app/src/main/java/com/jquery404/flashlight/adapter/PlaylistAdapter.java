package com.jquery404.flashlight.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jquery404.flashlight.R;
import com.jquery404.flashlight.custom.OnSongSelectedListener;
import com.jquery404.flashlight.main.MainActivity;

import java.util.ArrayList;

import com.jquery404.flashlight.databinding.PlaylistItemBinding;

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
        holder.binding.songTitle.setText(song.getName());
        holder.binding.songDuration.setText(song.getDuration());
        holder.binding.songArtist.setText(song.getArtist());
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }


    class PlaylistHolder extends RecyclerView.ViewHolder {
        private PlaylistItemBinding binding;
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

            binding = PlaylistItemBinding.bind(itemView);
            binding.cardView.setOnClickListener(v -> {
                Song song = songs.get(getAdapterPosition());
                listener.onSongSelected(song);
                dialog.dismiss();
            });
        }
    }
}
