package com.jquery404.flashlight.adapter;

/**
 * Created by Faisal on 7/26/17.
 */

public class Song {
    private String name, path, bitrate, duration, artist, nextSong;
    private byte[] albumArt;


    public Song() {

    }

    public Song(String name, String path, String bitrate, String duration, String artist) {
        this.name = name;
        this.path = path;
        this.bitrate = bitrate;
        this.duration = duration;
        this.artist = artist;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getBitrate() {
        return bitrate;
    }

    public void setBitrate(String bitrate) {
        this.bitrate = bitrate;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }


    public String getNextSong() {
        return nextSong;
    }

    public void setNextSong(String nextSong) {
        this.nextSong = nextSong;
    }

    public byte[] getAlbumArt() {
        return albumArt;
    }

    public void setAlbumArt(byte[] albumArt) {
        this.albumArt = albumArt;
    }
}
