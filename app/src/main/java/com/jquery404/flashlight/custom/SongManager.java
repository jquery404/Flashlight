package com.jquery404.flashlight.custom;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Debug;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.jquery404.flashlight.adapter.Song;
import com.jquery404.flashlight.main.MainActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by faisal on 11/10/2017.
 */

public class SongManager {

    public interface SongScanCallback {
        void onSongFound(Song song);
        void onScanComplete(int totalSongs);
    }

    private static final String PREFS_NAME = "FlashlightPrefs";
    private static final String KEY_SONGS_CACHE = "songs_cache";
    private static final String KEY_LAST_SCAN = "last_scan_time";
    private static final long CACHE_VALIDITY = 24 * 60 * 60 * 1000; // 24 hours
    
    private ArrayList<Song> songsList = new ArrayList<>();
    private int currentSongPos = 0;
    private Context context;

    public SongManager() {
        if (songsList == null) {
            songsList = new ArrayList<>();
        }
    }
    
    public SongManager(Context context) {
        this.context = context;
        if (songsList == null) {
            songsList = new ArrayList<>();
        }
    }

    public int getCurrentSongPos() {
        return currentSongPos;
    }

    public void setCurrentSongPos(int currentSongPos) {
        this.currentSongPos = currentSongPos;
    }

    public ArrayList<Song> getPlayList() {
        return getPlayList(false);
    }
    
    public ArrayList<Song> getPlayList(boolean forceRescan) {
        return getPlayList(forceRescan, null);
    }
    
    public ArrayList<Song> getPlayList(boolean forceRescan, SongScanCallback callback) {
        // Try to load from cache first if not forcing rescan
        if (!forceRescan && context != null) {
            ArrayList<Song> cachedSongs = loadFromCache();
            if (cachedSongs != null && !cachedSongs.isEmpty()) {
                Log.d("SongManager", "Loaded " + cachedSongs.size() + " songs from cache");
                songsList = cachedSongs;
                if (callback != null) {
                    callback.onScanComplete(cachedSongs.size());
                }
                return songsList;
            }
        }
        
        // If no cache or force rescan, scan file system
        Log.d("SongManager", "Scanning file system for songs...");
        songsList.clear();
        
        try {
            File home = Environment.getExternalStorageDirectory();
            if (home == null || !home.exists()) {
                Log.e("SongManager", "External storage directory not available");
                return songsList;
            }
            
            File[] listFiles = home.listFiles();
            if (listFiles != null && listFiles.length > 0) {
                for (File file : listFiles) {
                    if (file.isDirectory()) {
                        scanDirectory(file, callback);
                    } else {
                        addSongToList(file, callback);
                    }
                }
            }
            
            File musicDir = new File(home, "Music");
            if (musicDir.exists() && musicDir.isDirectory()) {
                scanDirectory(musicDir, callback);
            }
            
            File downloadDir = new File(home, "Download");
            if (downloadDir.exists() && downloadDir.isDirectory()) {
                scanDirectory(downloadDir, callback);
            }
        } catch (SecurityException e) {
            Log.e("SongManager", "Permission denied accessing storage", e);
        } catch (Exception e) {
            Log.e("SongManager", "Error getting playlist", e);
        }
        
        // Save to cache after successful scan
        if (context != null && !songsList.isEmpty()) {
            saveToCache(songsList);
        }
        
        if (callback != null) {
            callback.onScanComplete(songsList.size());
        }

        return songsList;
    }
    

    private void scanDirectory(File directory, SongScanCallback callback) {
        if (directory != null) {
            File[] listFiles = directory.listFiles();
            if (listFiles != null && listFiles.length > 0) {
                for (File file : listFiles) {
                    if (file.isDirectory()) {
                        scanDirectory(file, callback);
                    } else {
                        addSongToList(file, callback);
                    }
                }
            }
        }
    }


    private void addSongToList(File song, SongScanCallback callback) {
        if (song == null || !song.exists() || !song.canRead()) {
            return;
        }
        
        String fileName = song.getName().toLowerCase();
        if (!fileName.endsWith(".ogg") && !fileName.endsWith(".mp3")) {
            return;
        }

        String out = "";
        MediaMetadataRetriever mmr = null;
        
        try {
            mmr = new MediaMetadataRetriever();
            mmr.setDataSource(song.getPath());
            String title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            String bitrate = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);
            String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            String artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            
            if (artist == null)
                artist = "Unknown";
            if (title == null)
                title = song.getName().substring(0, song.getName().lastIndexOf('.'));
            
            if (duration == null || duration.isEmpty()) {
                duration = "0";
            }
            
            long dur = Long.parseLong(duration);
            String seconds = String.valueOf((dur % 60000) / 1000);
            String minutes = String.valueOf(dur / 60000);
            if (seconds.length() == 1) {
                out = ("0" + minutes + ":0" + seconds);
            } else {
                out = ("0" + minutes + ":" + seconds);
            }
            
            String bitrateStr = "0";
            if (bitrate != null && !bitrate.isEmpty()) {
                try {
                    bitrateStr = "" + (Integer.parseInt(bitrate) / 1000);
                } catch (NumberFormatException e) {
                    bitrateStr = "0";
                }
            }
            
            Song mSong = new Song(title, song.getPath(), bitrateStr, out, artist);
            mSong.setAlbumArt(mmr.getEmbeddedPicture());
            songsList.add(mSong);
            
            if (callback != null) {
                callback.onSongFound(mSong);
            }
        } catch (Exception e) {
            Log.e("SongManager", "Error reading song metadata: " + song.getPath(), e);
        } finally {
            if (mmr != null) {
                try {
                    mmr.release();
                } catch (Exception e) {
                    Log.e("SongManager", "Error releasing MediaMetadataRetriever", e);
                }
            }
        }
    }

    public Song playNextSong() {
        Song song;

        if (currentSongPos < (songsList.size() - 1)) {
            song = songsList.get(currentSongPos + 1);
            currentSongPos = currentSongPos + 1;
        } else {
            song = songsList.get(0);
            currentSongPos = 0;
        }

        song.setNextSong(getNextSong(currentSongPos));

        return song;
    }

    public Song playPrevSong() {
        Song song;

        if (currentSongPos > 0) {
            song = songsList.get(currentSongPos - 1);
            currentSongPos = currentSongPos - 1;
        } else {
            song = songsList.get(songsList.size() - 1);
            currentSongPos = songsList.size() - 1;
        }

        song.setNextSong(getNextSong(currentSongPos));

        return song;
    }

    public String getNextSong(int index) {
        Song song;

        if (index < (songsList.size() - 1)) {
            song = songsList.get(index + 1);
        } else {
            song = songsList.get(0);
        }

        return song.getName();
    }
    
    // ============================================================================================
    // Cache Methods
    // ============================================================================================
    
    private void saveToCache(ArrayList<Song> songs) {
        if (context == null) return;
        
        try {
            JSONArray jsonArray = new JSONArray();
            for (Song song : songs) {
                JSONObject jsonSong = new JSONObject();
                jsonSong.put("name", song.getName());
                jsonSong.put("path", song.getPath());
                jsonSong.put("bitrate", song.getBitrate());
                jsonSong.put("duration", song.getDuration());
                jsonSong.put("artist", song.getArtist());
                if (song.getAlbumArt() != null) {
                    jsonSong.put("albumArt", Base64.encodeToString(song.getAlbumArt(), Base64.DEFAULT));
                }
                jsonArray.put(jsonSong);
            }
            
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            prefs.edit()
                .putString(KEY_SONGS_CACHE, jsonArray.toString())
                .putLong(KEY_LAST_SCAN, System.currentTimeMillis())
                .apply();
            
            Log.d("SongManager", "Saved " + songs.size() + " songs to cache");
        } catch (Exception e) {
            Log.e("SongManager", "Error saving cache", e);
        }
    }
    
    private ArrayList<Song> loadFromCache() {
        if (context == null) return null;
        
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            
            // Check cache validity (optional - remove if you want cache to persist indefinitely)
            long lastScan = prefs.getLong(KEY_LAST_SCAN, 0);
            long cacheAge = System.currentTimeMillis() - lastScan;
            if (cacheAge > CACHE_VALIDITY) {
                Log.d("SongManager", "Cache expired (age: " + (cacheAge / 1000 / 60) + " minutes)");
                return null;
            }
            
            String jsonString = prefs.getString(KEY_SONGS_CACHE, null);
            if (jsonString == null) return null;
            
            JSONArray jsonArray = new JSONArray(jsonString);
            ArrayList<Song> songs = new ArrayList<>();
            
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonSong = jsonArray.getJSONObject(i);
                // Skip file existence check for instant load
                // Files will be validated when user tries to play them
                Song song = new Song(
                    jsonSong.getString("name"),
                    jsonSong.getString("path"),
                    jsonSong.getString("bitrate"),
                    jsonSong.getString("duration"),
                    jsonSong.getString("artist")
                );
                if (jsonSong.has("albumArt")) {
                    song.setAlbumArt(Base64.decode(jsonSong.getString("albumArt"), Base64.DEFAULT));
                }
                songs.add(song);
            }
            
            return songs;
        } catch (Exception e) {
            Log.e("SongManager", "Error loading cache", e);
            return null;
        }
    }
    
    public boolean hasCache() {
        if (context == null) return false;
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String cachedData = prefs.getString(KEY_SONGS_CACHE, null);
        return cachedData != null && !cachedData.isEmpty();
    }
    
    public void clearCache() {
        if (context == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_SONGS_CACHE).remove(KEY_LAST_SCAN).apply();
        Log.d("SongManager", "Cache cleared");
    }
}
