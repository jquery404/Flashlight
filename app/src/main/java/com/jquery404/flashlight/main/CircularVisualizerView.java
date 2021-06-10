package com.jquery404.flashlight.main;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.jquery404.flashlight.R;

/**
 * Created by faisal on 16/11/2017.
 */

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
        //mForePaint.setColor(ContextCompat.getColor(context, R.color.colorVisualizeTop));
    }

    public void updateVisualizer(byte[] bytes) {
        mBytes = bytes;
        invalidate();
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mBytes == null || !active)
            return;

        if (mCycleColor) {
            cycleColor();
        }

        if (mPoints == null || mPoints.length < mBytes.length * 4) {
            mPoints = new float[mBytes.length * 4];
        }

        mRect.set(0, 0, getWidth(), getHeight());


        for (int i = 0; i < mBytes.length - 1; i++) {
            float[] cartPoint = {
                    (float) i / (mBytes.length - 1),
                    mRect.height() / 2 + ((byte) (mBytes[0] + 128)) * (mRect.height() / 2) / 64
                    // i
            };

            float[] polarPoint = toPolar(cartPoint, mRect);
            mPoints[i * 4] = polarPoint[0];
            mPoints[i * 4 + 1] = polarPoint[1];

            float[] cartPoint2 = {
                    (float) (i + 1) / (mBytes.length - 1),
                    mRect.height() / 2 + ((byte) (mBytes[i + 1] + 128)) * (mRect.height() / 2) / 64
                    // i + 1
            };

            float[] polarPoint2 = toPolar(cartPoint2, mRect);
            mPoints[i * 4 + 2] = polarPoint2[0];
            mPoints[i * 4 + 3] = polarPoint2[1];
        }

        canvas.drawLines(mPoints, mForePaint);

        // Controls the pulsing rate
        modulation += 0.4;
        colorCounter += 128;
    }

    float modulation = 0;
    float aggresive = 0.7f;

    private float[] toPolar(float[] cartesian, Rect rect) {
        double cX = rect.width() / 2;
        double cY = rect.height() / 2;
        double angle = (cartesian[0]) * 2 * Math.PI;
        /*double radius = (((rect.width() / 2) * (1 - aggresive) +
                aggresive * cartesian[1] / 2) * (1.2 + Math.sin(modulation)) / 2.2);*/
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
