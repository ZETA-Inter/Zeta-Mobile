package com.example.zeta_mobile.company;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.zeta_mobile.R;

public class CountdownProgressView extends View {

    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arc = new RectF();

    private float strokePx;
    private int trackColor;
    private int progressColor;
    private String centerText = "05:00";
    private float fraction = 1f; // 1.0 = cheio, 0.0 = vazio

    public CountdownProgressView(Context c) { this(c, null); }
    public CountdownProgressView(Context c, @Nullable AttributeSet a) { this(c, a, 0); }
    public CountdownProgressView(Context c, @Nullable AttributeSet a, int s) {
        super(c, a, s);
        float dp12 = getResources().getDisplayMetrics().density * 12f;
        strokePx = dp12;
        trackColor = ContextCompat.getColor(c, R.color.divider);
        progressColor = ContextCompat.getColor(c, R.color.primary_dark);

        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeCap(Paint.Cap.ROUND);
        trackPaint.setStrokeWidth(strokePx);
        trackPaint.setColor(trackColor);

        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setStrokeWidth(strokePx);
        progressPaint.setColor(progressColor);

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(ContextCompat.getColor(c, R.color.text_primary));
        textPaint.setTextSize(getResources().getDisplayMetrics().scaledDensity * 14f);
        textPaint.setFakeBoldText(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        float pad = strokePx / 2f + getPaddingLeft();
        arc.set(pad, pad, w - pad - getPaddingRight(), h - pad - getPaddingBottom());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // trilha
        canvas.drawArc(arc, -90, 360, false, trackPaint);
        // progresso restante
        canvas.drawArc(arc, -90, 360f * fraction, false, progressPaint);
        // texto central
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        float cy = getHeight()/2f - (fm.ascent + fm.descent)/2f;
        canvas.drawText(centerText, getWidth()/2f, cy, textPaint);
    }

    /** 0..1 (1 = cheio). */
    public void setProgressFraction(float f) {
        this.fraction = Math.max(0f, Math.min(1f, f));
        invalidate();
    }

    public void setCenterText(String t) {
        this.centerText = t;
        invalidate();
    }
}
