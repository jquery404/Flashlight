package com.jquery404.flashlight.main;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class FlashLightManager {
    private static final String TAG = "FlashLightManager";
    
    private CameraManager cameraManager;
    private String cameraId;
    private boolean isFlashOn = false;
    private Context context;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable turnOffRunnable = new Runnable() {
        @Override
        public void run() {
            turnOffFlash();
        }
    };

    public FlashLightManager(Context context) {
        this.context = context;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            try {
                if (cameraManager != null) {
                    String[] cameraIds = cameraManager.getCameraIdList();
                    for (String id : cameraIds) {
                        android.hardware.camera2.CameraCharacteristics characteristics =
                                cameraManager.getCameraCharacteristics(id);
                        Boolean flashAvailable = characteristics.get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE);
                        if (flashAvailable != null && flashAvailable) {
                            cameraId = id;
                            break;
                        }
                    }
                }
            } catch (CameraAccessException e) {
                Log.e(TAG, "Error accessing camera", e);
            }
        }
    }

    public boolean hasFlash() {
        return cameraId != null;
    }

    public void turnOnFlash() {
        if (cameraId == null || cameraManager == null) {
            Log.w(TAG, "Cannot turn on flash: camera not initialized");
            return;
        }
        
        // Remove pending turn off if any
        handler.removeCallbacks(turnOffRunnable);

        if (isFlashOn) {
            return;
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                cameraManager.setTorchMode(cameraId, true);
                isFlashOn = true;
                Log.d(TAG, "Flash turned on");
            } catch (CameraAccessException e) {
                Log.e(TAG, "Error turning on flash", e);
                isFlashOn = false;
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error turning on flash", e);
                isFlashOn = false;
            }
        }
    }

    public void turnOffFlash() {
        if (cameraId == null || cameraManager == null) {
            Log.w(TAG, "Cannot turn off flash: camera not initialized");
            return;
        }
        
        if (!isFlashOn) {
            return;
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                cameraManager.setTorchMode(cameraId, false);
                isFlashOn = false;
                Log.d(TAG, "Flash turned off");
            } catch (CameraAccessException e) {
                Log.e(TAG, "Error turning off flash", e);
                isFlashOn = false;
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error turning off flash", e);
                isFlashOn = false;
            }
        }
    }
    
    // Strobe effect: Turn on then auto-off after delay
    public void flash() {
        turnOnFlash();
        handler.postDelayed(turnOffRunnable, 50); // 50ms flash
    }

    public boolean isFlashOn() {
        return isFlashOn;
    }

    public void release() {
        handler.removeCallbacks(turnOffRunnable);
        if (isFlashOn) {
            turnOffFlash();
        }
        cameraManager = null;
        cameraId = null;
    }
}
