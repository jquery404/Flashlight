package com.jquery404.flashlight.main;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;
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
    private float amplitude = 0;
    boolean mTop = false;
    private float colorCounter = 0;
    private LinearGradient gradientColor;


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
        gradientColor = new LinearGradient(0, 0, 0, getHeight(), Color.RED, Color.YELLOW, Shader.TileMode.MIRROR);
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
        double sum = 0;
        for (int i = 0; i < audioData.length / 2; i++) {
            double y = (audioData[i * 2] | audioData[i * 2 + 1] << 8) / 32768.0;
            sum += y * y;
        }
        double rms = Math.sqrt(sum / audioData.length / 2);
        return 20.0 * Math.log10(rms);

        /*for (int i = 0; i < audioData.length / 2; i++) {
            double y = (audioData[i * 2] | audioData[i * 2 + 1] << 8) / 32768.0;
            amplitude += y * y;
        }
        double rms = Math.sqrt(amplitude / audioData.length / 2);
        return 20.0 * Math.log10(rms);*/
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mBytes == null)
            return;

        if (mPoints == null || mPoints.length < mBytes.length * 4)
            mPoints = new float[mBytes.length * 4];

        mRect.set(0, 0, getWidth(), getHeight());
        mForePaint.setShader(gradientColor);

        int mDivisions = 2;

        // (0,0) (100,0) // (0,100) (100,100)
        for (int i = 0; i < mBytes.length / mDivisions; i++) {
            mPoints[i * 4] = 0;
            mPoints[i * 4 + 1] = 100;
            mPoints[i * 4 + 2] = mRect.width();
            mPoints[i * 4 + 3] = 100;
        }


        /*
        Utils.cycleColor(colorCounter)
        colorCounter += 0.03;*/


        /* wave generation */
        /*for (int i = 0; i < mBytes.length - 1; i++) {
            mPoints[i * 4] = mRect.width() * i / (mBytes.length - 1);
            mPoints[i * 4 + 1] = mRect.height() / 2 + ((byte) (mBytes[i] + 128)) * (mRect.height() / 2) / 128;
            mPoints[i * 4 + 2] = mRect.width() * (i + 1) / (mBytes.length - 1);
            mPoints[i * 4 + 3] = mRect.height() / 2 + ((byte) (mBytes[i + 1] + 128)) * (mRect.height() / 2) / 128;
        }*/



        /* bar graph */
        /*for (int i = 0; i < mBytes.length / mDivisions; i++) {
            mPoints[i * 4] = i * 4 * mDivisions;
            mPoints[i * 4 + 2] = i * 4 * mDivisions;
            byte rfk = mBytes[mDivisions * i];
            byte ifk = mBytes[mDivisions * i + 1];
            float magnitude = (rfk * rfk + ifk * ifk);
            int dbValue = (int) (10 * Math.log10(magnitude));

            if (mTop) {
                mPoints[i * 4 + 1] = 0;
                mPoints[i * 4 + 3] = (dbValue * 2 - 10);
            } else {
                mPoints[i * 4 + 1] = mRect.height();
                mPoints[i * 4 + 3] = mRect.height() - (dbValue * 2 - 10);
            }
        }


        float accumulator = 0;
        for (int i = 0; i < mBytes.length - 1; i++) {
            accumulator += Math.abs(mBytes[i]);
        }

        float amp = accumulator / (128 * mBytes.length);
        if (amp > amplitude) {
            amplitude = amp;
            canvas.drawLines(mPoints, mForePaint);
        } else {
            amplitude *= 0.99;
            canvas.drawLines(mPoints, mForePaint);
        }*/

        /* bar */
        /*for (int i = 0; i < mBytes.length / 2; i++) {
            mPoints[i * 4] = i * 8;
            mPoints[i * 4 + 1] = 0;
            mPoints[i * 4 + 2] = i * 8;
            byte rfk = mBytes[2 * i];
            byte ifk = mBytes[2 * i + 1];
            float magnitude = (float) (rfk * rfk + ifk * ifk);
            int dbValue = (int) (10 * Math.log10(magnitude));
            mPoints[i * 4 + 3] = (float) (dbValue * 7);
        }*/


        canvas.drawLines(mPoints, mForePaint);
    }
}
