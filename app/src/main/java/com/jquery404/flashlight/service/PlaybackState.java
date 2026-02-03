package com.jquery404.flashlight.service;

import com.jquery404.flashlight.adapter.Song;

/**
 * Immutable value object representing the authoritative state of playback.
 * This is the single source of truth for the UI and other consumers.
 */
public class PlaybackState {
    
    public enum State {
        PLAYING,
        PAUSED,
        BUFFERING,
        STOPPED,
        ERROR
    }

    public enum RepeatMode {
        OFF,
        ONE,
        ALL
    }

    public enum ShuffleMode {
        OFF,
        ON
    }

    private final State state;
    private final Song song;
    private final long position; // Position in ms at last update
    private final long lastUpdateTimestamp; // System.currentTimeMillis() when position was updated
    private final long duration; // Total duration in ms
    private final RepeatMode repeatMode;
    private final ShuffleMode shuffleMode;
    
    // Default empty state
    public static final PlaybackState EMPTY = new PlaybackState(State.STOPPED, null, 0, 0, 0, RepeatMode.OFF, ShuffleMode.OFF);

    public PlaybackState(State state, Song song, long position, long lastUpdateTimestamp, long duration, RepeatMode repeatMode, ShuffleMode shuffleMode) {
        this.state = state;
        this.song = song;
        this.position = position;
        this.lastUpdateTimestamp = lastUpdateTimestamp;
        this.duration = duration;
        this.repeatMode = repeatMode;
        this.shuffleMode = shuffleMode;
    }

    public State getState() {
        return state;
    }

    public boolean isPlaying() {
        return state == State.PLAYING;
    }

    public boolean isActivePlayback() {
        return state == State.PLAYING || state == State.BUFFERING;
    }

    public Song getSong() {
        return song;
    }

    /**
     * Returns the authoritative position at the last update time.
     * To get the real-time position, use {@link #getCurrentPosition(long)}.
     */
    public long getPosition() {
        return position;
    }

    public long getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    public long getDuration() {
        return duration;
    }

    public RepeatMode getRepeatMode() {
        return repeatMode;
    }

    public ShuffleMode getShuffleMode() {
        return shuffleMode;
    }

    /**
     * Calculates the estimated current position based on the system clock.
     * This avoids the need for polling from the UI.
     * 
     * @param currentTime The current system time (System.currentTimeMillis())
     * @return The estimated position in milliseconds
     */
    public long getCurrentPosition(long currentTime) {
        if (state != State.PLAYING) {
            return position;
        }
        
        long delta = currentTime - lastUpdateTimestamp;
        long estimatedPosition = position + delta;
        
        if (duration > 0 && estimatedPosition > duration) {
            if (repeatMode == RepeatMode.ONE) {
                // For Repeat One, we estimate the wrap-around
                return estimatedPosition % duration;
            } else {
                // Clamp to duration for other modes
                return duration;
            }
        }
        
        return estimatedPosition;
    }
    
    @Override
    public String toString() {
        return "PlaybackState{" +
                "state=" + state +
                ", song=" + (song != null ? song.getName() : "null") +
                ", position=" + position +
                ", duration=" + duration +
                '}';
    }
}
