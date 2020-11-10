package com.tiamosu.calendarview.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import com.tiamosu.calendarview.entity.Calendar
import com.tiamosu.calendarview.utils.CalendarUtil

/**
 * 默认高仿魅族日历布局
 *
 * @author tiamosu
 * @date 2020/5/25.
 */
class DefaultMonthView(context: Context) : MonthView(context) {
    private val textPaint = Paint()
    private val schemeBasicPaint = Paint()
    private val radio: Float
    private val padding: Int
    private val schemeBaseLine: Float

    init {
        textPaint.textSize = CalendarUtil.dipToPx(context, 8f).toFloat()
        textPaint.color = -0x1
        textPaint.isAntiAlias = true
        textPaint.isFakeBoldText = true
        schemeBasicPaint.isAntiAlias = true
        schemeBasicPaint.style = Paint.Style.FILL
        schemeBasicPaint.textAlign = Paint.Align.CENTER
        schemeBasicPaint.color = -0x12acad
        schemeBasicPaint.isFakeBoldText = true
        radio = CalendarUtil.dipToPx(getContext(), 7f).toFloat()
        padding = CalendarUtil.dipToPx(getContext(), 4f)
        val metrics = schemeBasicPaint.fontMetrics
        schemeBaseLine =
            radio - metrics.descent + (metrics.bottom - metrics.top) / 2 + CalendarUtil.dipToPx(
                getContext(),
                1f
            )
    }

    /**
     * @param canvas    canvas
     * @param calendar  日历日历calendar
     * @param x         日历Card x起点坐标
     * @param y         日历Card y起点坐标
     * @param hasScheme hasScheme 非标记的日期
     * @return true 则绘制onDrawScheme，因为这里背景色不是是互斥的
     */
    override fun onDrawSelected(
        canvas: Canvas,
        calendar: Calendar,
        x: Int,
        y: Int,
        hasScheme: Boolean
    ): Boolean {
        selectedItemPaint.style = Paint.Style.FILL
        canvas.drawRect(
            x + padding.toFloat(), y + padding.toFloat(),
            x + itemWidth - padding.toFloat(), y + itemHeight - padding.toFloat(), selectedItemPaint
        )
        return true
    }

    override fun onDrawScheme(canvas: Canvas, calendar: Calendar, x: Int, y: Int) {
        schemeBasicPaint.color = calendar.schemeColor
        canvas.drawCircle(
            x + itemWidth - padding - radio / 2,
            y + padding + radio,
            radio,
            schemeBasicPaint
        )
        canvas.drawText(
            calendar.scheme,
            x + itemWidth - padding - radio / 2 - getTextWidth(calendar.scheme) / 2,
            y + padding + schemeBaseLine, textPaint
        )
    }

    /**
     * 获取字体的宽
     * @param text text
     * @return return
     */
    private fun getTextWidth(text: String?): Float {
        return textPaint.measureText(text)
    }

    override fun onDrawText(
        canvas: Canvas,
        calendar: Calendar,
        x: Int,
        y: Int,
        hasScheme: Boolean,
        isSelected: Boolean
    ) {
        val cx = x + itemWidth / 2
        val top = y - itemHeight / 6
        when {
            isSelected -> {
                canvas.drawText(
                    java.lang.String.valueOf(calendar.day), cx.toFloat(), textBaseLine + top,
                    selectTextPaint
                )
                canvas.drawText(
                    calendar.lunar,
                    cx.toFloat(),
                    textBaseLine + y + itemHeight / 10,
                    selectedLunarTextPaint
                )
            }
            hasScheme -> {
                canvas.drawText(
                    java.lang.String.valueOf(calendar.day), cx.toFloat(), textBaseLine + top,
                    when {
                        calendar.isCurrentDay -> curDayTextPaint
                        calendar.isCurrentMonth -> schemeTextPaint
                        else -> otherMonthTextPaint
                    }
                )
                canvas.drawText(
                    calendar.lunar, cx.toFloat(), textBaseLine + y + itemHeight / 10,
                    if (calendar.isCurrentDay) curDayLunarTextPaint else schemeLunarTextPaint
                )
            }
            else -> {
                canvas.drawText(
                    java.lang.String.valueOf(calendar.day), cx.toFloat(), textBaseLine + top,
                    when {
                        calendar.isCurrentDay -> curDayTextPaint
                        calendar.isCurrentMonth -> curMonthTextPaint
                        else -> otherMonthTextPaint
                    }
                )
                canvas.drawText(
                    calendar.lunar, cx.toFloat(), textBaseLine + y + itemHeight / 10,
                    when {
                        calendar.isCurrentDay -> curDayLunarTextPaint
                        calendar.isCurrentMonth -> curMonthLunarTextPaint
                        else -> otherMonthLunarTextPaint
                    }
                )
            }
        }
    }
}