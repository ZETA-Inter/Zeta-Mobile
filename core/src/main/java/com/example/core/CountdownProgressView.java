package com.example.core;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class CountdownProgressView extends View {

    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arc = new RectF();

    private float strokePx;
    private int trackColor;
    private int progressColor;
    private int textColor;

    private String centerText = "05:00";
    private float fraction = 1f; // 1.0 = cheio, 0.0 = vazio

    public CountdownProgressView(Context c) { this(c, null); }
    public CountdownProgressView(Context c, @Nullable AttributeSet a) { this(c, a, 0); }
    public CountdownProgressView(Context c, @Nullable AttributeSet a, int s) {
        super(c, a, s);

        // defaults
        strokePx     = getResources().getDisplayMetrics().density * 12f;
        trackColor   = ContextCompat.getColor(c, R.color.divider);
        progressColor= ContextCompat.getColor(c, R.color.primary_dark);
        textColor    = ContextCompat.getColor(c, R.color.text_primary);

        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeCap(Paint.Cap.ROUND);
        trackPaint.setStrokeWidth(strokePx);
        trackPaint.setColor(trackColor);

        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setStrokeWidth(strokePx);
        progressPaint.setColor(progressColor);

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(textColor);
        textPaint.setTextSize(getResources().getDisplayMetrics().scaledDensity * 14f);
        textPaint.setFakeBoldText(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        float padLeft   = getPaddingLeft()   + strokePx / 2f;
        float padTop    = getPaddingTop()    + strokePx / 2f;
        float padRight  = getPaddingRight()  + strokePx / 2f;
        float padBottom = getPaddingBottom() + strokePx / 2f;
        arc.set(padLeft, padTop, w - padRight, h - padBottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // trilha completa
        canvas.drawArc(arc, -90, 360, false, trackPaint);
        // arco de progresso
        canvas.drawArc(arc, -90, 360f * fraction, false, progressPaint);
        // texto central
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        float cy = getHeight() / 2f - (fm.ascent + fm.descent) / 2f;
        canvas.drawText(centerText, getWidth() / 2f, cy, textPaint);
    }

    // ========= API pública existente =========
    public void setProgressFraction(float f) {
        this.fraction = Math.max(0f, Math.min(1f, f));
        invalidate();
    }

    public void setCenterText(String t) {
        this.centerText = t != null ? t : "";
        invalidate();
    }

    public void updateTimer(float fraction, String text) {
        setProgressFraction(fraction);
        setCenterText(text);
    }

    // ========= NOVOS SETTERS (Opção A) =========
    /** Cor do anel/progresso (o que se move). */
    public void setRingColor(@ColorInt int color) {
        this.progressColor = color;
        this.progressPaint.setColor(color);
        invalidate();
    }

    /** Cor da trilha (fundo do círculo). */
    public void setTrackColor(@ColorInt int color) {
        this.trackColor = color;
        this.trackPaint.setColor(color);
        invalidate();
    }

    /** Cor do texto central. */
    public void setTextColorCompat(@ColorInt int color) {
        this.textColor = color;
        this.textPaint.setColor(color);
        invalidate();
    }

    /** (Opcional) Ajusta a espessura do traço em pixels. */
    public void setStrokeWidthPx(float px) {
        if (px <= 0f) return;
        this.strokePx = px;
        trackPaint.setStrokeWidth(px);
        progressPaint.setStrokeWidth(px);
        requestLayout(); // arco depende do stroke (padding visual)
        invalidate();
    }

    /** (Opcional) Ajusta a espessura do traço em dp. */
    public void setStrokeWidthDp(float dp) {
        float px = dp * getResources().getDisplayMetrics().density;
        setStrokeWidthPx(px);
    }
}
