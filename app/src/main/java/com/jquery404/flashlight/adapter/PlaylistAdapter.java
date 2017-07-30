package com.jquery404.flashlight.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jquery404.flashlight.R;

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

    public PlaylistAdapter(Context context, ArrayList<Song> songs) {
        this.context = context;
        this.songs = songs;
    }

    @Override
    public PlaylistHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.playlist_item, viewGroup, false);
        return new PlaylistHolder(v, context, songs);
    }

    @Override
    public void onBindViewHolder(PlaylistHolder holder, int i) {
        Song song = songs.get(i);
        holder.songTitle.setText(song.getName());
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }


    class PlaylistHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.song_title)
        TextView songTitle;

        ArrayList<Song> songs = new ArrayList<>();
        Context context;

        PlaylistHolder(View itemView, Context context, ArrayList<Song> songs) {
            super(itemView);
            this.context = context;
            this.songs = songs;

            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.card_view)
        void onClick() {
            Song song = songs.get(getAdapterPosition());
            Intent returnIntent = new Intent();
            returnIntent.putExtra("songPath", song.getPath());
            returnIntent.putExtra("songBPM", song.getBitrate());
            ((Activity) context).setResult(Activity.RESULT_OK, returnIntent);
            ((Activity) context).finish();
        }
    }
}
