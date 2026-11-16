package com.nooxe.file.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.nooxe.file.data.StorageState

class DonutView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.parseColor("#40FFFFFF")
        strokeCap = Paint.Cap.ROUND
    }
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    private val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#B3FFFFFF")
        textAlign = Paint.Align.CENTER
    }

    private var percent: Float = 0f
    private var label: String = ""

    fun setProgress(usedMB: Int, totalMB: Int, labelText: String) {
        percent = (usedMB.toFloat() / totalMB.toFloat()).coerceIn(0f, 1f)
        label = labelText
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        val size = minOf(w, h)
        val stroke = size * 0.13f
        trackPaint.strokeWidth = stroke
        progressPaint.strokeWidth = stroke
        textPaint.textSize = size * 0.17f
        subPaint.textSize = size * 0.09f

        val r = (size - stroke) / 2f
        val cx = w / 2f
        val cy = h / 2f
        val oval = RectF(cx - r, cy - r, cx + r, cy + r)

        canvas.drawArc(oval, 0f, 360f, false, trackPaint)

        val sweep = 360f * percent
        val g = android.graphics.LinearGradient(
            cx - r, cy - r, cx + r, cy + r,
            Color.parseColor("#FFC58F"), Color.parseColor("#FF6B3D"),
            android.graphics.Shader.TileMode.CLAMP
        )
        progressPaint.shader = g
        canvas.drawArc(oval, -90f, sweep, false, progressPaint)

        val fm = textPaint.fontMetrics
        canvas.drawText(label, cx, cy - (fm.ascent + fm.descent) / 2f, textPaint)
    }
}
