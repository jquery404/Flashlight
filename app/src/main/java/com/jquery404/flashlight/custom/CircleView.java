package com.jquery404.flashlight.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import com.jquery404.flashlight.R;


/**
 * Created by Faisal on 7/26/17.
 */

public class CircleView extends View {

    private Paint _paintCenter;
    private Paint _paint;
    private RectF rectF;


    public CircleView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        rectF = new RectF();
        _paint = new Paint();
        _paint.setColor(ContextCompat.getColor(context, R.color.colorBottomHalfCircle));
        _paint.setAntiAlias(true);

        _paintCenter = new Paint();

        _paintCenter.setColor(ContextCompat.getColor(context, R.color.colorTopHalfCircle));
        _paintCenter.setAntiAlias(true);
    }


    @Override
    protected void onDraw(final Canvas canvas) {
        final float height = canvas.getHeight();
        final float width = canvas.getWidth();

        float radius = width > height ? height * 0.5F : width * 0.5F;

        final float centerX = canvas.getWidth() * 0.5F;
        final float centerY = canvas.getHeight() * 0.5F;

        canvas.drawCircle(centerX, centerY, radius, _paint);
        rectF.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
        canvas.drawArc(rectF, 0, 180, true, _paintCenter);
    }

}