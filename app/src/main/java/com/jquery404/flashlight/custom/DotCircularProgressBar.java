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
    private int activeColor = Color.parseColor("#FF1744");
    private int inactiveColor = Color.parseColor("#3F51B5");
    
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

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

    public void setProgress(int progress) {
        this.progress = Math.min(progress, maxProgress);
        invalidate();
    }

    public int getMaxProgress() {
        return maxProgress;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        float radius = (Math.min(centerX, centerY) - dotRadius) * 0.95f;

        float progressFraction = (float) progress / maxProgress;
        int activeDots = (int) (progressFraction * dotCount);

        for (int i = 0; i < dotCount; i++) {
            double angle = Math.toRadians((double) i * 360 / dotCount - 90);
            float x = (float) (centerX + radius * Math.cos(angle));
            float y = (float) (centerY + radius * Math.sin(angle));

            if (i < activeDots) {
                paint.setColor(activeColor);
            } else {
                paint.setColor(inactiveColor);
                paint.setAlpha(100);
            }

            canvas.drawCircle(x, y, dotRadius, paint);
        }
    }
}
