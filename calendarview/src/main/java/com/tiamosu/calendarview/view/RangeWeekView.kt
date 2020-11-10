package com.tiamosu.calendarview.view

import android.content.Context
import android.graphics.Canvas
import android.view.View
import com.tiamosu.calendarview.entity.Calendar
import com.tiamosu.calendarview.utils.CalendarUtil

/**
 * 范围选择周视图
 *
 * @author tiamosu
 * @date 2020/5/25.
 */
abstract class RangeWeekView(context: Context) : BaseWeekView(context) {

    /**
     * 绘制日历文本
     *
     * @param canvas canvas
     */
    override fun onDraw(canvas: Canvas) {
        if (items.isEmpty()) {
            return
        }
        itemWidth = (width - 2 * viewDelegate.calendarPadding) / 7
        onPreviewHook()
        for (i in 0..6) {
            val x = i * itemWidth + viewDelegate.calendarPadding
            onLoopStart(x)
            val calendar = items[i]
            val isSelected = isCalendarSelected(calendar)
            val isPreSelected = isSelectPreCalendar(calendar)
            val isNextSelected = isSelectNextCalendar(calendar)
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
        if (viewDelegate.selectedStartRangeCalendar == null) {
            return false
        }
        if (onCalendarIntercept(calendar)) {
            return false
        }
        return if (viewDelegate.selectedEndRangeCalendar == null) {
            calendar.compareTo(viewDelegate.selectedStartRangeCalendar) == 0
        } else calendar >= viewDelegate.selectedStartRangeCalendar &&
                calendar <= viewDelegate.selectedEndRangeCalendar
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
            viewDelegate.calendarRangeSelectListener?.onCalendarSelectOutOfRange(calendar)
            return
        }

        //优先判断各种直接return的情况，减少代码深度
        if (viewDelegate.selectedStartRangeCalendar != null && viewDelegate.selectedEndRangeCalendar == null) {
            val minDiffer = CalendarUtil.differ(calendar, viewDelegate.selectedStartRangeCalendar)
            if (minDiffer >= 0 && viewDelegate.minSelectRange != -1 && viewDelegate.minSelectRange > minDiffer + 1) {
                viewDelegate.calendarRangeSelectListener?.onSelectOutOfRange(calendar, true)
                return
            } else if (viewDelegate.maxSelectRange != -1 && viewDelegate.maxSelectRange <
                CalendarUtil.differ(calendar, viewDelegate.selectedStartRangeCalendar) + 1
            ) {
                viewDelegate.calendarRangeSelectListener?.onSelectOutOfRange(calendar, false)
                return
            }
        }
        if (viewDelegate.selectedStartRangeCalendar == null || viewDelegate.selectedEndRangeCalendar != null) {
            viewDelegate.selectedStartRangeCalendar = calendar
            viewDelegate.selectedEndRangeCalendar = null
        } else {
            val compare = calendar.compareTo(viewDelegate.selectedStartRangeCalendar)
            if (viewDelegate.minSelectRange == -1 && compare <= 0) {
                viewDelegate.selectedStartRangeCalendar = calendar
                viewDelegate.selectedEndRangeCalendar = null
            } else if (compare < 0) {
                viewDelegate.selectedStartRangeCalendar = calendar
                viewDelegate.selectedEndRangeCalendar = null
            } else if (compare == 0 &&
                viewDelegate.minSelectRange == 1
            ) {
                viewDelegate.selectedEndRangeCalendar = calendar
            } else {
                viewDelegate.selectedEndRangeCalendar = calendar
            }
        }
        currentItem = items.indexOf(calendar)
        viewDelegate.innerDateSelectedListener?.onWeekDateSelected(calendar, true)
        if (parentLayout != null) {
            val i = CalendarUtil.getWeekFromDayInMonth(calendar, viewDelegate.weekStart)
            parentLayout?.updateSelectWeek(i)
        }
        viewDelegate.calendarRangeSelectListener?.onCalendarRangeSelect(
            calendar,
            viewDelegate.selectedEndRangeCalendar != null
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
     * @return 上一个日期是否选中
     */
    protected fun isSelectPreCalendar(calendar: Calendar): Boolean {
        val preCalendar = CalendarUtil.getPreCalendar(calendar)
        viewDelegate.updateCalendarScheme(preCalendar)
        return viewDelegate.selectedStartRangeCalendar != null &&
                isCalendarSelected(preCalendar)
    }

    /**
     * 下一个日期是否选中
     *
     * @param calendar 当前日期
     * @return 下一个日期是否选中
     */
    protected fun isSelectNextCalendar(calendar: Calendar): Boolean {
        val nextCalendar = CalendarUtil.getNextCalendar(calendar)
        viewDelegate.updateCalendarScheme(nextCalendar)
        return viewDelegate.selectedStartRangeCalendar != null &&
                isCalendarSelected(nextCalendar)
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