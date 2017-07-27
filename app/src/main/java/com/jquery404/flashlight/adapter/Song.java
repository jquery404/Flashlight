package com.jquery404.flashlight.adapter;

/**
 * Created by Faisal on 7/26/17.
 */

public class Song {
    private String name, path;

    public Song() {

    }

    public Song(String name, String path) {
        this.name = name;
        this.path = path;
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
}
