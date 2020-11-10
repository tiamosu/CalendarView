package com.tiamosu.calendarview.view

import android.content.Context
import android.graphics.Canvas
import android.view.View
import com.tiamosu.calendarview.entity.Calendar
import com.tiamosu.calendarview.utils.CalendarUtil

/**
 * 周视图，因为日历UI采用热插拔实现，所以这里必须继承实现，达到UI一致即可
 *
 * @author tiamosu
 * @date 2020/5/25.
 */
abstract class WeekView(context: Context) : BaseWeekView(context) {

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
        for (i in items.indices) {
            val x = i * itemWidth + viewDelegate.calendarPaddingLeft
            onLoopStart(x)
            val calendar = items[i]
            val isSelected = i == currentItem
            val hasScheme = calendar.hasScheme()
            if (hasScheme) {
                var isDrawSelected = false //是否继续绘制选中的onDrawScheme
                if (isSelected) {
                    isDrawSelected = onDrawSelected(canvas, calendar, x, true)
                }
                if (isDrawSelected || !isSelected) {
                    //将画笔设置为标记颜色
                    schemePaint.color =
                        if (calendar.schemeColor != 0) calendar.schemeColor else viewDelegate.schemeThemeColor
                    onDrawScheme(canvas, calendar, x)
                }
            } else {
                if (isSelected) {
                    onDrawSelected(canvas, calendar, x, false)
                }
            }
            onDrawText(canvas, calendar, x, hasScheme, isSelected)
        }
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
            viewDelegate.calendarSelectListener?.onCalendarOutOfRange(calendar)
            return
        }
        currentItem = items.indexOf(calendar)
        viewDelegate.innerDateSelectedListener?.onWeekDateSelected(calendar, true)
        val i = CalendarUtil.getWeekFromDayInMonth(calendar, viewDelegate.weekStart)
        parentLayout?.updateSelectWeek(i)
        viewDelegate.calendarSelectListener?.onCalendarSelect(calendar, true)
        invalidate()
    }

    override fun onLongClick(v: View): Boolean {
        if (viewDelegate.calendarLongClickListener == null) {
            return false
        }
        if (!isClick) {
            return false
        }
        val calendar = index ?: return false
        if (onCalendarIntercept(calendar)) {
            viewDelegate.calendarInterceptListener?.onCalendarInterceptClick(calendar, true)
            return true
        }
        val isCalendarInRange = isInRange(calendar)
        if (!isCalendarInRange) {
            viewDelegate.calendarLongClickListener?.onCalendarLongClickOutOfRange(calendar)
            return true
        }
        if (viewDelegate.isPreventLongPressedSelected) { //如果启用拦截长按事件不选择日期
            viewDelegate.calendarLongClickListener?.onCalendarLongClick(calendar)
            return true
        }
        currentItem = items.indexOf(calendar)
        viewDelegate.indexCalendar = viewDelegate.selectedCalendar
        viewDelegate.innerDateSelectedListener?.onWeekDateSelected(calendar, true)
        val i = CalendarUtil.getWeekFromDayInMonth(calendar, viewDelegate.weekStart)
        parentLayout?.updateSelectWeek(i)
        viewDelegate.calendarSelectListener?.onCalendarSelect(calendar, true)
        viewDelegate.calendarLongClickListener?.onCalendarLongClick(calendar)
        invalidate()
        return true
    }

    /**
     * 绘制选中的日期
     *
     * @param canvas    canvas
     * @param calendar  日历日历calendar
     * @param x         日历Card x起点坐标
     * @param hasScheme hasScheme 非标记的日期
     * @return 是否绘制 onDrawScheme
     */
    protected abstract fun onDrawSelected(
        canvas: Canvas,
        calendar: Calendar,
        x: Int,
        hasScheme: Boolean
    ): Boolean

    /**
     * 绘制标记的日期
     *
     * @param canvas   canvas
     * @param calendar 日历calendar
     * @param x        日历Card x起点坐标
     */
    protected abstract fun onDrawScheme(canvas: Canvas, calendar: Calendar, x: Int)

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