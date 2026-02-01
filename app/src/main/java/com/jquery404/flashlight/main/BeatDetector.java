package com.jquery404.flashlight.main;

import android.util.Log;

public class BeatDetector {
    private static final String TAG = "BeatDetector";
    
    private static final float BEAT_THRESHOLD = 1.3f;  // Increased from 0.6 to be more sensitive
    private static final float ENERGY_DECAY = 0.98f;   // Faster decay to adapt quicker
    private static final int BEAT_HOLD_TIME = 150;     // Reduced from 200ms
    private static final float VARIANCE_DECAY = 0.98f;
    
    private float instantEnergy = 0;
    private float averageEnergy = 0;
    private float variance = 0;
    private long lastBeatTime = 0;
    private boolean isBeat = false;
    
    private float smoothedIntensity = 0.5f;
    private static final float SMOOTHING_FACTOR = 0.3f;
    
    public boolean detectBeat(byte[] fftData) {
        if (fftData == null || fftData.length < 2) {
            return false;
        }
        
        instantEnergy = calculateEnergy(fftData);
        
        if (averageEnergy == 0) {
            averageEnergy = instantEnergy;
        } else {
            averageEnergy = averageEnergy * ENERGY_DECAY + instantEnergy * (1 - ENERGY_DECAY);
        }
        
        // Calculate threshold with reduced strictness
        // Use only average energy, not variance (which can make it too strict)
        float threshold = averageEnergy * BEAT_THRESHOLD;
        
        long currentTime = System.currentTimeMillis();
        boolean beatDetected = instantEnergy > threshold && (currentTime - lastBeatTime) > BEAT_HOLD_TIME;
        
        if (beatDetected) {
            Log.d(TAG, String.format("BEAT! Energy: %.2f, Avg: %.2f, Threshold: %.2f", 
                instantEnergy, averageEnergy, threshold));
            lastBeatTime = currentTime;
            isBeat = true;
            return true;
        }
        
        isBeat = false;
        return false;
    }
    
    private float calculateEnergy(byte[] fftData) {
        float sum = 0;
        int samplesToAnalyze = Math.min(fftData.length / 4, 32);
        
        for (int i = 0; i < samplesToAnalyze; i++) {
            if (i * 2 + 1 < fftData.length) {
                byte rfk = fftData[i * 2];
                byte ifk = fftData[i * 2 + 1];
                float magnitude = (rfk * rfk + ifk * ifk);
                sum += magnitude;
            }
        }
        
        return sum / samplesToAnalyze;
    }
    
    public float getSmoothedIntensity() {
        return smoothedIntensity;
    }
    
    public void updateIntensity(float rawIntensity) {
        rawIntensity = Math.max(0, Math.min(1, rawIntensity));
        
        smoothedIntensity = smoothedIntensity * (1 - SMOOTHING_FACTOR) + rawIntensity * SMOOTHING_FACTOR;
    }
    
    public float getInstantEnergy() {
        return instantEnergy;
    }
    
    public void reset() {
        instantEnergy = 0;
        averageEnergy = 0;
        variance = 0;
        lastBeatTime = 0;
        isBeat = false;
        smoothedIntensity = 0.5f;
    }
}
