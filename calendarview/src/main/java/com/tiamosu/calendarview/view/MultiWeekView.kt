package com.tiamosu.calendarview.view

import android.content.Context
import android.graphics.Canvas
import android.view.View
import com.tiamosu.calendarview.entity.Calendar
import com.tiamosu.calendarview.utils.CalendarUtil

/**
 * 多选周视图
 *
 * @author tiamosu
 * @date 2020/5/25.
 */
abstract class MultiWeekView(context: Context) : BaseWeekView(context) {

    /**
     * 绘制日历文本
     *
     * @param canvas canvas
     */
    override fun onDraw(canvas: Canvas) {
        if (items.isEmpty()) {
            return
        }
        itemWidth =
            (width - viewDelegate.calendarPaddingLeft - viewDelegate.calendarPaddingRight) / 7

        onPreviewHook()
        for (i in 0..6) {
            val x = i * itemWidth + viewDelegate.calendarPaddingLeft
            onLoopStart(x)

            val calendar = items[i]
            val isSelected = isCalendarSelected(calendar)
            val isPreSelected = isSelectPreCalendar(calendar, i)
            val isNextSelected = isSelectNextCalendar(calendar, i)
            val hasScheme = calendar.hasScheme()
            if (hasScheme) {
                var isDrawSelected = false //是否继续绘制选中的onDrawScheme
                if (isSelected) {
                    isDrawSelected =
                        onDrawSelected(canvas, calendar, x, true, isPreSelected, isNextSelected)
                }
                if (isDrawSelected || !isSelected) {
                    //将画笔设置为标记颜色
                    schemePaint.color =
                        if (calendar.schemeColor != 0) calendar.schemeColor else viewDelegate.schemeThemeColor
                    onDrawScheme(canvas, calendar, x, isSelected)
                }
            } else {
                if (isSelected) {
                    onDrawSelected(canvas, calendar, x, false, isPreSelected, isNextSelected)
                }
            }
            onDrawText(canvas, calendar, x, hasScheme, isSelected)
        }
    }

    /**
     * 日历是否被选中
     *
     * @param calendar calendar
     * @return 日历是否被选中
     */
    protected fun isCalendarSelected(calendar: Calendar): Boolean {
        return !onCalendarIntercept(calendar) && viewDelegate.selectedCalendars.containsKey(calendar.toString())
    }

    override fun onClick(v: View) {
        if (!isClick) {
            return
        }
        val calendar = index ?: return
        if (onCalendarIntercept(calendar)) {
            viewDelegate.calendarInterceptListener?.onCalendarInterceptClick(calendar, true)
            return
        }
        if (!isInRange(calendar)) {
            viewDelegate.calendarMultiSelectListener?.onCalendarMultiSelectOutOfRange(calendar)
            return
        }
        val key = calendar.toString()
        if (viewDelegate.selectedCalendars.containsKey(key)) {
            viewDelegate.selectedCalendars.remove(key)
        } else {
            if (viewDelegate.selectedCalendars.size >= viewDelegate.maxMultiSelectSize) {
                viewDelegate.calendarMultiSelectListener?.onMultiSelectOutOfSize(
                    calendar,
                    viewDelegate.maxMultiSelectSize
                )
                return
            }
            viewDelegate.selectedCalendars[key] = calendar
        }
        currentItem = items.indexOf(calendar)
        viewDelegate.innerDateSelectedListener?.onWeekDateSelected(calendar, true)
        if (parentLayout != null) {
            val i = CalendarUtil.getWeekFromDayInMonth(calendar, viewDelegate.weekStart)
            parentLayout?.updateSelectWeek(i)
        }
        viewDelegate.calendarMultiSelectListener?.onCalendarMultiSelect(
            calendar,
            viewDelegate.selectedCalendars.size,
            viewDelegate.maxMultiSelectSize
        )
        invalidate()
    }

    override fun onLongClick(v: View): Boolean {
        return false
    }

    /**
     * 上一个日期是否选中
     *
     * @param calendar 当前日期
     * @param calendarIndex 当前位置
     * @return 上一个日期是否选中
     */
    protected fun isSelectPreCalendar(calendar: Calendar, calendarIndex: Int): Boolean {
        val preCalendar: Calendar
        if (calendarIndex == 0) {
            preCalendar = CalendarUtil.getPreCalendar(calendar)
            viewDelegate.updateCalendarScheme(preCalendar)
        } else {
            preCalendar = items[calendarIndex - 1]
        }
        return isCalendarSelected(preCalendar)
    }

    /**
     * 下一个日期是否选中
     *
     * @param calendar 当前日期
     * @param calendarIndex 当前位置
     * @return 下一个日期是否选中
     */
    protected fun isSelectNextCalendar(calendar: Calendar, calendarIndex: Int): Boolean {
        val nextCalendar: Calendar
        if (calendarIndex == items.lastIndex) {
            nextCalendar = CalendarUtil.getNextCalendar(calendar)
            viewDelegate.updateCalendarScheme(nextCalendar)
        } else {
            nextCalendar = items[calendarIndex + 1]
        }
        return isCalendarSelected(nextCalendar)
    }

    /**
     * 绘制选中的日期
     *
     * @param canvas         canvas
     * @param calendar       日历日历calendar
     * @param x              日历Card x起点坐标
     * @param hasScheme      hasScheme 非标记的日期
     * @param isSelectedPre  上一个日期是否选中
     * @param isSelectedNext 下一个日期是否选中
     * @return 是否绘制 onDrawScheme
     */
    protected abstract fun onDrawSelected(
        canvas: Canvas, calendar: Calendar, x: Int, hasScheme: Boolean,
        isSelectedPre: Boolean, isSelectedNext: Boolean
    ): Boolean

    /**
     * 绘制标记的日期
     *
     * @param canvas     canvas
     * @param calendar   日历calendar
     * @param x          日历Card x起点坐标
     * @param isSelected 是否选中
     */
    protected abstract fun onDrawScheme(
        canvas: Canvas,
        calendar: Calendar,
        x: Int,
        isSelected: Boolean
    )

    /**
     * 绘制日历文本
     *
     * @param canvas     canvas
     * @param calendar   日历calendar
     * @param x          日历Card x起点坐标
     * @param hasScheme  是否是标记的日期
     * @param isSelected 是否选中
     */
    protected abstract fun onDrawText(
        canvas: Canvas,
        calendar: Calendar,
        x: Int,
        hasScheme: Boolean,
        isSelected: Boolean
    )
}