package com.tiamosu.calendarview.view

import android.content.Context
import android.graphics.Canvas
import android.view.View
import com.tiamosu.calendarview.delegate.CalendarViewDelegate
import com.tiamosu.calendarview.entity.Calendar
import com.tiamosu.calendarview.utils.CalendarUtil

/**
 * 范围选择月视图
 *
 * @author tiamosu
 * @date 2020/5/25.
 */
abstract class RangeMonthView(context: Context) : BaseMonthView(context) {

    override fun onDraw(canvas: Canvas) {
        if (lineCount == 0) {
            return
        }
        itemWidth =
            (width - viewDelegate.calendarPaddingLeft - viewDelegate.calendarPaddingRight) / 7

        onPreviewHook()
        val count = lineCount * 7
        var d = 0
        for (i in 0 until lineCount) {
            for (j in 0..6) {
                val calendar = items[d]
                if (viewDelegate.monthViewShowMode == CalendarViewDelegate.MODE_ONLY_CURRENT_MONTH) {
                    if (d > items.size - nextDiff) {
                        return
                    }
                    if (!calendar.isCurrentMonth) {
                        ++d
                        continue
                    }
                } else if (viewDelegate.monthViewShowMode == CalendarViewDelegate.MODE_FIT_MONTH) {
                    if (d >= count) {
                        return
                    }
                }
                draw(canvas, calendar, d, i, j)
                ++d
            }
        }
    }

    /**
     * 开始绘制
     *
     * @param canvas   canvas
     * @param calendar 对应日历
     * @param calendarIndex 当前位置
     * @param i        i
     * @param j        j
     */
    private fun draw(canvas: Canvas, calendar: Calendar, calendarIndex: Int, i: Int, j: Int) {
        val x = j * itemWidth + viewDelegate.calendarPaddingLeft
        val y = i * itemHeight
        onLoopStart(x, y)

        val isSelected = isCalendarSelected(calendar)
        val hasScheme = calendar.hasScheme()
        val isPreSelected = isSelectPreCalendar(calendar, calendarIndex)
        val isNextSelected = isSelectNextCalendar(calendar, calendarIndex)
        if (hasScheme) {
            //标记的日子
            var isDrawSelected = false //是否继续绘制选中的onDrawScheme
            if (isSelected) {
                isDrawSelected =
                    onDrawSelected(canvas, calendar, x, y, true, isPreSelected, isNextSelected)
            }
            if (isDrawSelected || !isSelected) {
                //将画笔设置为标记颜色
                schemePaint.color =
                    if (calendar.schemeColor != 0) calendar.schemeColor else viewDelegate.schemeThemeColor
                onDrawScheme(canvas, calendar, x, y, true)
            }
        } else {
            if (isSelected) {
                onDrawSelected(canvas, calendar, x, y, false, isPreSelected, isNextSelected)
            }
        }
        onDrawText(canvas, calendar, x, y, hasScheme, isSelected)
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
        } else calendar >= viewDelegate.selectedStartRangeCalendar
                && calendar <= viewDelegate.selectedEndRangeCalendar
    }

    override fun onClick(v: View) {
        if (!isClick) {
            return
        }
        val calendar = getIndex() ?: return
        if (viewDelegate.monthViewShowMode == CalendarViewDelegate.MODE_ONLY_CURRENT_MONTH
            && !calendar.isCurrentMonth
        ) {
            return
        }
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
        if (!calendar.isCurrentMonth) {
            val cur = monthViewPager.currentItem
            val position = if (currentItem < 7) cur - 1 else cur + 1
            monthViewPager.currentItem = position
        }
        viewDelegate.innerDateSelectedListener?.onMonthDateSelected(calendar, true)
        if (calendar.isCurrentMonth) {
            parentLayout?.updateSelectPosition(items.indexOf(calendar))
        } else {
            parentLayout?.updateSelectWeek(
                CalendarUtil.getWeekFromDayInMonth(calendar, viewDelegate.weekStart)
            )
        }
        viewDelegate.calendarRangeSelectListener?.onCalendarRangeSelect(
            calendar, viewDelegate.selectedEndRangeCalendar != null
        )
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
        return viewDelegate.selectedStartRangeCalendar != null && isCalendarSelected(preCalendar)
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
        return viewDelegate.selectedStartRangeCalendar != null && isCalendarSelected(nextCalendar)
    }

    /**
     * 绘制选中的日期
     *
     * @param canvas         canvas
     * @param calendar       日历日历calendar
     * @param x              日历Card x起点坐标
     * @param y              日历Card y起点坐标
     * @param hasScheme      hasScheme 非标记的日期
     * @param isSelectedPre  上一个日期是否选中
     * @param isSelectedNext 下一个日期是否选中
     * @return 是否继续绘制onDrawScheme，true or false
     */
    protected abstract fun onDrawSelected(
        canvas: Canvas, calendar: Calendar, x: Int, y: Int,
        hasScheme: Boolean, isSelectedPre: Boolean, isSelectedNext: Boolean
    ): Boolean

    /**
     * 绘制标记的日期,这里可以是背景色，标记色什么的
     *
     * @param canvas     canvas
     * @param calendar   日历calendar
     * @param x          日历Card x起点坐标
     * @param y          日历Card y起点坐标
     * @param isSelected 是否选中
     */
    protected abstract fun onDrawScheme(
        canvas: Canvas,
        calendar: Calendar,
        x: Int,
        y: Int,
        isSelected: Boolean
    )

    /**
     * 绘制日历文本
     *
     * @param canvas     canvas
     * @param calendar   日历calendar
     * @param x          日历Card x起点坐标
     * @param y          日历Card y起点坐标
     * @param hasScheme  是否是标记的日期
     * @param isSelected 是否选中
     */
    protected abstract fun onDrawText(
        canvas: Canvas, calendar: Calendar,
        x: Int, y: Int, hasScheme: Boolean, isSelected: Boolean,
    )
}