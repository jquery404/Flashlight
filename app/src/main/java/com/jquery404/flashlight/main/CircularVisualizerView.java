package com.jquery404.flashlight.main;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import androidx.core.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.jquery404.flashlight.R;

public class CircularVisualizerView extends View {

    private byte[] mBytes, mFFTBytes;
    private double dbAmp;
    private float[] mPoints;
    private Rect mRect = new Rect();
    private Paint mForePaint = new Paint();
    private Context context;
    private boolean active = true;

    private boolean mCycleColor = true;


    public CircularVisualizerView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public CircularVisualizerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public CircularVisualizerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        mBytes = null;

        mForePaint.setStrokeWidth(8f);
        mForePaint.setAntiAlias(true);
    }

    public void updateVisualizer(byte[] bytes) {
        mBytes = bytes;
        invalidate();
    }

    public enum Mode {
        BLOB, BARS
    }
    
    public void setActive(boolean active) {
        this.active = active;
        invalidate();
    }

    private Mode currentMode = Mode.BLOB;

    public void setMode(Mode mode) {
        this.currentMode = mode;
        invalidate();
    }

    public void toggleMode() {
        this.currentMode = (currentMode == Mode.BLOB) ? Mode.BARS : Mode.BLOB;
        invalidate();
    }

    private android.graphics.Path mPath = new android.graphics.Path();

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mBytes == null || !active)
            return;

        if (currentMode == Mode.BLOB) {
            drawBlob(canvas);
        } else {
            drawBars(canvas);
        }

        modulation += 0.05;
    }

    private void drawBlob(Canvas canvas) {
        mForePaint.setStyle(Paint.Style.FILL);
        mForePaint.setColor(Color.parseColor("#00BCD4"));
        mForePaint.setAlpha(180);

        mRect.set(0, 0, getWidth(), getHeight());
        mPath.reset();

        float centerX = mRect.width() / 2f;
        float centerY = mRect.height() / 2f;
        
        int points = 120; 
        
        for (int i = 0; i <= points; i++) {
            int index = (i * (mBytes.length - 1)) / points;
            float rawValue = (byte) (mBytes[index] + 128);
            
            double angle = (double) i * 2 * Math.PI / points;
            
            float waveFactor = (float) (Math.sin(modulation + angle * 2) * 10);
            
            float baseRadius = mRect.width() * 0.30f;
            float audioRadius = (rawValue / 255f) * (mRect.width() * 0.15f);
            float radius = baseRadius + audioRadius + waveFactor;

            float x = (float) (centerX + radius * Math.sin(angle));
            float y = (float) (centerY + radius * Math.cos(angle));

            if (i == 0) {
                mPath.moveTo(x, y);
            } else {
                mPath.lineTo(x, y);
            }
        }
        mPath.close();
        canvas.drawPath(mPath, mForePaint);
    }

    private void drawBars(Canvas canvas) {
        mForePaint.setStyle(Paint.Style.STROKE);
        mForePaint.setStrokeWidth(5f);
        mForePaint.setColor(Color.parseColor("#00BCD4"));
        
        mRect.set(0, 0, getWidth(), getHeight());
        float centerX = mRect.width() / 2f;
        float centerY = mRect.height() / 2f;
        float baseRadius = mRect.width() * 0.32f;
        
        int bars = 60;
        for (int i = 0; i < bars; i++) {
            int index = (i * (mBytes.length - 1)) / bars;
            float rawValue = (byte) (mBytes[index] + 128);
            float barHeight = (rawValue / 255f) * (mRect.width() * 0.12f);
            
            double angle = (double) i * 2 * Math.PI / bars;
            float startX = (float) (centerX + baseRadius * Math.sin(angle));
            float startY = (float) (centerY + baseRadius * Math.cos(angle));
            float endX = (float) (centerX + (baseRadius + barHeight) * Math.sin(angle));
            float endY = (float) (centerY + (baseRadius + barHeight) * Math.cos(angle));
            
            canvas.drawLine(startX, startY, endX, endY, mForePaint);
        }
    }

    float modulation = 0;
    float aggresive = 0.7f;

    private float[] toPolar(float[] cartesian, Rect rect) {
        double cX = rect.width() / 2;
        double cY = rect.height() / 2;
        double angle = (cartesian[0]) * 2 * Math.PI;
        double radius = ((rect.width() / 2) * (1 - aggresive) + aggresive * cartesian[1] / 2);
        float[] out = {
                (float) (cX + radius * Math.sin(angle)),
                (float) (cY + radius * Math.cos(angle))
        };
        return out;
    }

    private float colorCounter = 0;

    private void cycleColor() {
        int r = (int) Math.floor(128 * (Math.sin(colorCounter) + 1));
        int g = (int) Math.floor(128 * (Math.sin(colorCounter + 2) + 1));
        int b = (int) Math.floor(128 * (Math.sin(colorCounter + 4) + 1));
        mForePaint.setColor(Color.argb(128, r, g, b));
        colorCounter += 0.03;
    }

    public double clamp(double val, double min, double max) {
        return Math.min(Math.max(val, min), max);
    }

}
