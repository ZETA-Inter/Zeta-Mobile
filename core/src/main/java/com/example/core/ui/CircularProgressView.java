package com.example.core.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class CircularProgressView extends View {
    private Paint backgroundPaint;
    private Paint progressPaint;
    private Paint textPaint;
    private RectF rect;
    private float progress = 0;
    private int startColor = 0xFF563887; // roxo claro
    private int endColor = 0xFF9C89BE; // roxo escuro

    public CircularProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(0xFFE0E0E0);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(20f);
        backgroundPaint.setStrokeCap(Paint.Cap.ROUND);

        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(20f);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(0xFF000000);
        textPaint.setTextSize(40f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        rect = new RectF();
    }

    public void setProgress(float progress) {
        this.progress = Math.max(0, Math.min(progress, 100));
        invalidate();
    }

    public void setGradientColors(int startColor, int endColor) {
        this.startColor = startColor;
        this.endColor = endColor;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();

        float strokeWidth = 30f;
        progressPaint.setStrokeWidth(strokeWidth);
        backgroundPaint.setStrokeWidth(strokeWidth);

        float radius = Math.min(width, height) / 2f - strokeWidth / 2f;

        float cx = width / 2f;
        float cy = height / 2f;

        rect.set(cx - radius, cy - radius, cx + radius, cy + radius);

        canvas.drawArc(rect, 0, 360, false, backgroundPaint);

        SweepGradient gradient = new SweepGradient(
                cx, cy,
                new int[]{startColor, endColor},
                null
        );

        Matrix matrix = new Matrix();
        matrix.postRotate(-90, cx, cy);
        gradient.setLocalMatrix(matrix);

        progressPaint.setShader(gradient);
        progressPaint.setStrokeWidth(strokeWidth);

        progressPaint.setStrokeCap(Paint.Cap.BUTT);
        backgroundPaint.setStrokeCap(Paint.Cap.BUTT);

        float sweepAngle = -(progress / 100f) * 360f;
        canvas.drawArc(rect, -90, sweepAngle, false, progressPaint);

        canvas.drawText((int) progress + "%", cx, cy + (textPaint.getTextSize() / 3), textPaint);
    }
}
