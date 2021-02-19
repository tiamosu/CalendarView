package com.tiamosu.calendarview.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import com.tiamosu.calendarview.entity.Calendar
import com.tiamosu.calendarview.utils.CalendarUtil

/**
 * 默认高仿魅族周视图
 *
 * @author tiamosu
 * @date 2020/5/25.
 */
class DefaultWeekView(context: Context) : WeekView(context) {
    private val textPaint by lazy {
        Paint().apply {
            textSize = CalendarUtil.dipToPx(context, 8f).toFloat()
            color = -0x1
            isAntiAlias = true
            isFakeBoldText = true
        }
    }
    private val schemeBasicPaint by lazy {
        Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            textAlign = Paint.Align.CENTER
            color = -0x12acad
            isFakeBoldText = true
        }
    }
    private val radio by lazy { CalendarUtil.dipToPx(getContext(), 7f).toFloat() }
    private val padding by lazy { CalendarUtil.dipToPx(getContext(), 4f) }
    private val schemeBaseLine by lazy {
        val metrics = schemeBasicPaint.fontMetrics
        radio - metrics.descent + (metrics.bottom - metrics.top) / 2 +
                CalendarUtil.dipToPx(getContext(), 1f)
    }

    /**
     * 如果需要点击Scheme没有效果，则return true
     *
     * @param canvas    canvas
     * @param calendar  日历日历calendar
     * @param x         日历Card x起点坐标
     * @param hasScheme hasScheme 非标记的日期
     * @return true 则绘制onDrawScheme，因为这里背景色不是是互斥的
     */
    override fun onDrawSelected(
        canvas: Canvas,
        calendar: Calendar,
        x: Int,
        hasScheme: Boolean
    ): Boolean {
        selectedItemPaint.style = Paint.Style.FILL
        canvas.drawRect(
            x + padding.toFloat(),
            padding.toFloat(),
            x + itemWidth - padding.toFloat(),
            itemHeight - padding.toFloat(), selectedItemPaint
        )
        return true
    }

    override fun onDrawScheme(canvas: Canvas, calendar: Calendar, x: Int) {
        schemeBasicPaint.color = calendar.schemeColor
        canvas.drawCircle(
            x + itemWidth - padding - radio / 2,
            padding + radio,
            radio,
            schemeBasicPaint
        )
        canvas.drawText(
            calendar.scheme,
            x + itemWidth - padding - radio / 2 - getTextWidth(calendar.scheme) / 2,
            padding + schemeBaseLine, textPaint
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
        hasScheme: Boolean,
        isSelected: Boolean
    ) {
        val cx = x + itemWidth / 2
        val top = -itemHeight / 6
        when {
            isSelected -> {
                canvas.drawText(
                    java.lang.String.valueOf(calendar.day), cx.toFloat(), textBaseLine + top,
                    selectTextPaint
                )
                canvas.drawText(
                    calendar.lunar,
                    cx.toFloat(),
                    textBaseLine + itemHeight / 10,
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
                    calendar.lunar, cx.toFloat(), textBaseLine + itemHeight / 10,
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
                    calendar.lunar, cx.toFloat(), textBaseLine + itemHeight / 10,
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