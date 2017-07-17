package com.jquery404.flashlight.main;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by Faisal on 7/2/17.
 */

public class VisualizerView extends View {

    private byte[] mBytes, mFFTBytes;
    private double dbAmp;
    private float[] mPoints;
    private Rect mRect = new Rect();
    private Paint mForePaint = new Paint();

    public VisualizerView(Context context) {
        super(context);
        init();
    }

    public VisualizerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VisualizerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mBytes = null;
        mForePaint.setStrokeWidth(1f);
        mForePaint.setAntiAlias(true);
        mForePaint.setColor(Color.rgb(255, 255, 255));
    }

    public void updateVisualizer(byte[] bytes) {
        mBytes = bytes;
        invalidate();
    }

    public void updateVisualizerFFT(byte[] bytes) {
        dbAmp = computedbAmp(bytes);
        mFFTBytes = bytes;
        invalidate();
    }

    public double computedbAmp(byte[] audioData) {
        double amplitude = 0;
        for (int i = 0; i < audioData.length / 2; i++) {
            double y = (audioData[i * 2] | audioData[i * 2 + 1] << 8) / 32768.0;
            amplitude += y * y;
        }
        double rms = Math.sqrt(amplitude / audioData.length / 2);
        return 20.0 * Math.log10(rms);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mBytes == null)
            return;

        if (mPoints == null || mPoints.length < mBytes.length * 4)
            mPoints = new float[mBytes.length * 4];

        mRect.set(0, 0, getWidth(), getHeight());

        for (int i = 0; i < mBytes.length - 1; i++) {
            mPoints[i * 4] = mRect.width() * i / (mBytes.length - 1);
            mPoints[i * 4 + 1] = mRect.height() / 2 + ((byte) (mBytes[i] + 128)) * (mRect.height() / 2) / 128;
            mPoints[i * 4 + 2] = mRect.width() * (i + 1) / (mBytes.length - 1);
            mPoints[i * 4 + 3] = mRect.height() / 2 + ((byte) (mBytes[i + 1] + 128)) * (mRect.height() / 2) / 128;
        }

        canvas.drawLines(mPoints, mForePaint);
    }


}