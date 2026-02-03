package com.jquery404.flashlight.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class DotCircularProgressBar extends View {

    private int progress = 0;
    private int maxProgress = 100;
    private int dotCount = 60;
    private float dotRadius = 6f;
    private int activeColor = Color.parseColor("#FF1744"); // Red dot
    private int inactiveColor = Color.parseColor("#3F51B5"); // Inactive background (fallback)
    private int durationColor = Color.parseColor("#80DEEA"); // Light blue (Total duration)
    private int previewColor = Color.parseColor("#FFCCBC"); // Light orange/red (Preview)

    private float currentThicknessScale = 1.0f;
    private boolean isPreviewing = false;
    private int previewProgress = 0;
    
    private android.animation.ValueAnimator thicknessAnimator;
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public interface OnSeekListener {
        void onSeek(int progress, boolean fromUser);
        void onStartTracking();
        void onStopTracking(int finalProgress);
    }
    
    private OnSeekListener seekListener;

    public DotCircularProgressBar(Context context) {
        super(context);
        init();
    }

    public DotCircularProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint.setStyle(Paint.Style.FILL);
    }

    public void setOnSeekListener(OnSeekListener listener) {
        this.seekListener = listener;
    }

    public void setProgress(int progress) {
        if (!isPreviewing) {
            this.progress = Math.min(progress, maxProgress);
            invalidate();
        }
    }

    public int getMaxProgress() {
        return maxProgress;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        float radius = (Math.min(centerX, centerY) - (dotRadius * 1.5f)) * 0.95f;

        float progressFraction = (float) progress / maxProgress;
        int activeDots = (int) (progressFraction * dotCount);
        
        int previewActiveDots = 0;
        if (isPreviewing) {
            float previewFraction = (float) previewProgress / maxProgress;
            previewActiveDots = (int) (previewFraction * dotCount);
        }

        for (int i = 0; i < dotCount; i++) {
            double angle = Math.toRadians((double) i * 360 / dotCount - 90);
            float x = (float) (centerX + radius * Math.cos(angle));
            float y = (float) (centerY + radius * Math.sin(angle));

            float currentDotRadius = dotRadius * currentThicknessScale;

            if (isPreviewing) {
                // Determine color based on preview state
                if (i < Math.max(activeDots, previewActiveDots)) {
                     if (i < Math.min(activeDots, previewActiveDots)) {
                         paint.setColor(activeColor); // Overlap
                     } else if (i < previewActiveDots) {
                         paint.setColor(previewColor); // Preview extension
                     } else {
                         paint.setColor(activeColor);
                         paint.setAlpha(120); // Old position fading
                     }
                } else {
                    paint.setColor(durationColor);
                    paint.setAlpha(100);
                }
            } else {
                // Normal state
                if (i < activeDots) {
                    paint.setColor(activeColor);
                } else {
                    paint.setColor(durationColor);
                    paint.setAlpha(100);
                }
            }

            canvas.drawCircle(x, y, currentDotRadius, paint);
        }
    }

    @Override
    public boolean onTouchEvent(android.view.MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;

        // Check distance to ensure touch is near the ring
        float distance = (float) Math.sqrt(Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2));
        float radius = (Math.min(centerX, centerY));
        
        // Accept touches within a generous margin of the circle
        boolean isNearCircle = distance > radius * 0.5f && distance < radius * 1.2f;

        switch (event.getAction()) {
            case android.view.MotionEvent.ACTION_DOWN:
                if (isNearCircle) {
                    isPreviewing = true;
                    animateThickness(true);
                    updatePreviewFromTouch(x, y, centerX, centerY);
                    if (seekListener != null) seekListener.onStartTracking();
                    return true;
                }
                break;
            case android.view.MotionEvent.ACTION_MOVE:
                if (isPreviewing) {
                    updatePreviewFromTouch(x, y, centerX, centerY);
                    return true;
                }
                break;
            case android.view.MotionEvent.ACTION_UP:
            case android.view.MotionEvent.ACTION_CANCEL:
                if (isPreviewing) {
                    isPreviewing = false;
                    animateThickness(false);
                    if (seekListener != null) {
                        seekListener.onStopTracking(previewProgress);
                        seekListener.onSeek(previewProgress, true);
                    }
                    invalidate();
                    return true;
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    private void updatePreviewFromTouch(float x, float y, float centerX, float centerY) {
        double angle = Math.toDegrees(Math.atan2(y - centerY, x - centerX)) + 90;
        if (angle < 0) angle += 360;
        
        previewProgress = (int) ((angle / 360.0) * maxProgress);
        if (seekListener != null) seekListener.onSeek(previewProgress, true);
        invalidate();
    }

    private void animateThickness(boolean grow) {
        if (thicknessAnimator != null) thicknessAnimator.cancel();
        
        float start = currentThicknessScale;
        float end = grow ? 1.5f : 1.0f;
        
        thicknessAnimator = android.animation.ValueAnimator.ofFloat(start, end);
        thicknessAnimator.setDuration(300);
        thicknessAnimator.setInterpolator(new android.view.animation.DecelerateInterpolator());
        thicknessAnimator.addUpdateListener(animation -> {
            currentThicknessScale = (float) animation.getAnimatedValue();
            invalidate();
        });
        thicknessAnimator.start();
    }
}
