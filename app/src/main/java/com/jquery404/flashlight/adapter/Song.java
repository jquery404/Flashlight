package com.jquery404.flashlight.adapter;

/**
 * Created by Faisal on 7/26/17.
 */

public class Song {
    private String name, path, bitrate;

    public Song() {

    }

    public Song(String name, String path, String bitrate) {
        this.name = name;
        this.path = path;
        this.bitrate = bitrate;
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
}
