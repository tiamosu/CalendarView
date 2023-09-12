package com.tiamosu.calendarview.view

import android.content.Context
import android.graphics.Canvas
import com.tiamosu.calendarview.R
import com.tiamosu.calendarview.entity.Calendar
import com.tiamosu.calendarview.utils.CalendarUtil

/**
 * 默认年视图
 *
 * @author tiamosu
 * @date 2020/5/25.
 */
class DefaultYearView(context: Context) : YearView(context) {

    private val textPadding by lazy { CalendarUtil.dipToPx(context, 3f) }

    override fun onDrawMonth(
        canvas: Canvas,
        year: Int,
        month: Int,
        x: Int,
        y: Int,
        width: Int,
        height: Int
    ) {
        val text = context.resources.getStringArray(R.array.year_view_month_string_array)[month - 1]
        canvas.drawText(
            text,
            x + itemWidth / 2 - textPadding.toFloat(),
            y + monthTextBaseLine,
            monthTextPaint
        )
    }

    override fun onDrawWeek(canvas: Canvas, week: Int, x: Int, y: Int, width: Int, height: Int) {
        val text = context.resources.getStringArray(R.array.year_view_week_string_array)[week]
        canvas.drawText(
            text,
            x + width / 2.toFloat(),
            y + weekTextBaseLine,
            weekTextPaint
        )
    }

    override fun onDrawSelected(
        canvas: Canvas,
        calendar: Calendar,
        x: Int,
        y: Int,
        hasScheme: Boolean
    ): Boolean {
        return false
    }

    override fun onDrawScheme(canvas: Canvas, calendar: Calendar, x: Int, y: Int) {}

    override fun onDrawText(
        canvas: Canvas,
        calendar: Calendar,
        x: Int,
        y: Int,
        hasScheme: Boolean,
        isSelected: Boolean
    ) {
        val baselineY = textBaseLine + y
        val cx = x + itemWidth / 2
        when {
            isSelected -> {
                canvas.drawText(
                    calendar.day.toString(),
                    cx.toFloat(),
                    baselineY,
                    if (hasScheme) schemeTextPaint else selectTextPaint
                )
            }
            hasScheme -> {
                canvas.drawText(
                    calendar.day.toString(),
                    cx.toFloat(),
                    baselineY,
                    when {
                        calendar.isCurrentDay -> curDayTextPaint
                        calendar.isCurrentMonth -> schemeTextPaint
                        else -> otherMonthTextPaint
                    }
                )
            }
            else -> {
                canvas.drawText(
                    calendar.day.toString(), cx.toFloat(), baselineY,
                    when {
                        calendar.isCurrentDay -> curDayTextPaint
                        calendar.isCurrentMonth -> curMonthTextPaint
                        else -> otherMonthTextPaint
                    }
                )
            }
        }
    }
}