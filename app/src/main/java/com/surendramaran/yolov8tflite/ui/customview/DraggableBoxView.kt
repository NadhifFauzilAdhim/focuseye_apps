// File: DraggableBoxView.kt
package com.surendramaran.yolov8tflite.ui.customview

import com.surendramaran.yolov8tflite.R

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

class DraggableBoxView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val boxPaint = Paint().apply {
        color = Color.parseColor("#80FFFFFF")
        style = Paint.Style.FILL
    }
    private val borderPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }
    private val cornerPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }
    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 40f
        textAlign = Paint.Align.CENTER
    }

    private var boxRect = RectF(200f, 200f, 600f, 500f)
    private val cornerRadius = 30f
    private val touchSlop = 40f
    private var currentDragMode = DragMode.NONE
    private val lastTouchPoint = PointF()

    private enum class DragMode { NONE, MOVE, RESIZE_TL, RESIZE_TR, RESIZE_BL, RESIZE_BR }

    fun setInitialRect(rect: RectF) {
        boxRect.set(
            rect.left * width,
            rect.top * height,
            rect.right * width,
            rect.bottom * height
        )
        invalidate()
    }

    fun getNormalizedRect(): RectF {
        return RectF(
            boxRect.left / width,
            boxRect.top / height,
            boxRect.right / width,
            boxRect.bottom / height
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRect(boxRect, boxPaint)
        canvas.drawRect(boxRect, borderPaint)

        // Draw corners
        drawCorner(canvas, boxRect.left, boxRect.top)
        drawCorner(canvas, boxRect.right, boxRect.top)
        drawCorner(canvas, boxRect.left, boxRect.bottom)
        drawCorner(canvas, boxRect.right, boxRect.bottom)

        val text = "Posisikan kotak ini untuk menutupi area papan tulis"
        canvas.drawText(text, width / 2f, 100f, textPaint)
    }

    private fun drawCorner(canvas: Canvas, x: Float, y: Float) {
        canvas.drawCircle(x, y, cornerRadius, cornerPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                currentDragMode = getDragMode(x, y)
                lastTouchPoint.set(x, y)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (currentDragMode != DragMode.NONE) {
                    val dx = x - lastTouchPoint.x
                    val dy = y - lastTouchPoint.y
                    updateBoxRect(dx, dy)
                    lastTouchPoint.set(x, y)
                    invalidate()
                }
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                currentDragMode = DragMode.NONE
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun updateBoxRect(dx: Float, dy: Float) {
        when (currentDragMode) {
            DragMode.MOVE -> boxRect.offset(dx, dy)
            DragMode.RESIZE_TL -> boxRect.set(boxRect.left + dx, boxRect.top + dy, boxRect.right, boxRect.bottom)
            DragMode.RESIZE_TR -> boxRect.set(boxRect.left, boxRect.top + dy, boxRect.right + dx, boxRect.bottom)
            DragMode.RESIZE_BL -> boxRect.set(boxRect.left + dx, boxRect.top, boxRect.right, boxRect.bottom + dy)
            DragMode.RESIZE_BR -> boxRect.set(boxRect.left, boxRect.top, boxRect.right + dx, boxRect.bottom + dy)
            else -> {}
        }
        boxRect.left = boxRect.left.coerceIn(0f, width.toFloat())
        boxRect.top = boxRect.top.coerceIn(0f, height.toFloat())
        boxRect.right = boxRect.right.coerceIn(0f, width.toFloat())
        boxRect.bottom = boxRect.bottom.coerceIn(0f, height.toFloat())
    }

    private fun getDragMode(x: Float, y: Float): DragMode {
        if (isNearCorner(x, y, boxRect.left, boxRect.top)) return DragMode.RESIZE_TL
        if (isNearCorner(x, y, boxRect.right, boxRect.top)) return DragMode.RESIZE_TR
        if (isNearCorner(x, y, boxRect.left, boxRect.bottom)) return DragMode.RESIZE_BL
        if (isNearCorner(x, y, boxRect.right, boxRect.bottom)) return DragMode.RESIZE_BR
        if (boxRect.contains(x, y)) return DragMode.MOVE
        return DragMode.NONE
    }

    private fun isNearCorner(x1: Float, y1: Float, x2: Float, y2: Float): Boolean {
        return abs(x1 - x2) < touchSlop && abs(y1 - y2) < touchSlop
    }
}