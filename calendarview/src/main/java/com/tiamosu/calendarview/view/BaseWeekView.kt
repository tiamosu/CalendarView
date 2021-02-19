package com.tiamosu.calendarview.view

import android.content.Context
import com.tiamosu.calendarview.delegate.CalendarViewDelegate
import com.tiamosu.calendarview.entity.Calendar
import com.tiamosu.calendarview.utils.CalendarUtil

/**
 * 最基础周视图，因为日历UI采用热插拔实现，所以这里必须继承实现，达到UI一致即可
 * 可通过此扩展各种视图如：WeekView、RangeWeekView
 *
 * @author tiamosu
 * @date 2020/5/25.
 */
abstract class BaseWeekView(context: Context) : BaseView(context) {

    /**
     * 初始化周视图控件
     */
    fun setup(calendar: Calendar) {
        items = CalendarUtil.initCalendarForWeekView(calendar, viewDelegate)
        addSchemesFromMap()
        invalidate()
    }

    /**
     * 记录已经选择的日期
     */
    fun setSelectedCalendar(calendar: Calendar) {
        if (viewDelegate.selectMode == CalendarViewDelegate.SELECT_MODE_SINGLE
            && calendar != viewDelegate.selectedCalendar
        ) {
            return
        }
        currentItem = items.indexOf(calendar)
    }

    /**
     * 周视图切换点击默认位置
     */
    fun performClickCalendar(calendar: Calendar, isNotice: Boolean) {
        if (parentLayout == null || viewDelegate.innerDateSelectedListener == null || items.isEmpty()) {
            return
        }
        var week = CalendarUtil.getWeekViewIndexFromCalendar(calendar, viewDelegate.weekStart)
        if (items.contains(viewDelegate.currentDay)) {
            week = CalendarUtil.getWeekViewIndexFromCalendar(
                viewDelegate.currentDay,
                viewDelegate.weekStart
            )
        }
        var curIndex = week
        var currentCalendar = items[week]
        if (viewDelegate.selectMode != CalendarViewDelegate.SELECT_MODE_DEFAULT) {
            if (items.contains(viewDelegate.selectedCalendar)) {
                currentCalendar = viewDelegate.selectedCalendar
            } else {
                currentItem = -1
            }
        }
        if (!isInRange(currentCalendar)) {
            curIndex = getEdgeIndex(isMinRangeEdge(currentCalendar))
            currentCalendar = items[curIndex]
        }
        currentCalendar.isCurrentDay = currentCalendar == viewDelegate.currentDay
        viewDelegate.innerDateSelectedListener?.onWeekDateSelected(currentCalendar, false)

        val i = CalendarUtil.getWeekFromDayInMonth(currentCalendar, viewDelegate.weekStart)
        parentLayout?.updateSelectWeek(i)
        if (isNotice && viewDelegate.selectMode == CalendarViewDelegate.SELECT_MODE_DEFAULT) {
            viewDelegate.calendarSelectListener?.onCalendarSelect(currentCalendar, false)
        }
        parentLayout?.updateContentViewTranslateY()
        if (viewDelegate.selectMode == CalendarViewDelegate.SELECT_MODE_DEFAULT) {
            currentItem = curIndex
        }
        if (!viewDelegate.isShowYearSelectedLayout && calendar.year != viewDelegate.indexCalendar.year) {
            viewDelegate.yearChangeListener?.onYearChange(viewDelegate.indexCalendar.year)
        }
        viewDelegate.indexCalendar = currentCalendar
        invalidate()
    }

    /**
     * 是否是最小访问边界了
     *
     * @param calendar calendar
     * @return 是否是最小访问边界了
     */
    fun isMinRangeEdge(calendar: Calendar): Boolean {
        val c = java.util.Calendar.getInstance()
        c[viewDelegate.minYear, viewDelegate.minYearMonth - 1] = viewDelegate.minYearDay
        val minTime = c.timeInMillis
        c[calendar.year, calendar.month - 1] = calendar.day
        val curTime = c.timeInMillis
        return curTime < minTime
    }

    /**
     * 获得边界范围内下标
     *
     * @param isMinEdge isMinEdge
     * @return 获得边界范围内下标
     */
    fun getEdgeIndex(isMinEdge: Boolean): Int {
        for (i in items.indices) {
            val item = items[i]
            val isInRange = isInRange(item)
            if (isMinEdge && isInRange) {
                return i
            } else if (!isMinEdge && !isInRange) {
                return i - 1
            }
        }
        return if (isMinEdge) 6 else 0
    }

    /**
     * 获取点击的日历
     *
     * @return 获取点击的日历
     */
    protected val index: Calendar?
        get() {
            if (mX <= viewDelegate.calendarPaddingLeft || mX >= width - viewDelegate.calendarPaddingRight) {
                onClickCalendarPadding()
                return null
            }

            var indexX = (mX - viewDelegate.calendarPaddingLeft).toInt() / itemWidth
            if (indexX >= 7) {
                indexX = 6
            }
            val indexY = mY.toInt() / itemHeight
            val position = indexY * 7 + indexX // 选择项
            return if (position >= 0 && position < items.size) {
                items[position]
            } else null
        }

    private fun onClickCalendarPadding() {
        if (viewDelegate.clickCalendarPaddingListener == null) {
            return
        }
        var calendar: Calendar? = null
        var indexX = (mX - viewDelegate.calendarPaddingLeft).toInt() / itemWidth
        if (indexX >= 7) {
            indexX = 6
        }
        val indexY = mY.toInt() / itemHeight
        val position = indexY * 7 + indexX // 选择项
        if (position >= 0 && position < items.size) {
            calendar = items[position]
        }
        if (calendar == null) {
            return
        }
        viewDelegate.clickCalendarPaddingListener?.onClickCalendarPadding(
            mX, mY, false, calendar,
            getClickCalendarPaddingObject(mX, mY, calendar)
        )
    }

    /**
     * / **
     * 获取点击事件处的对象
     *
     * @param x                x
     * @param y                y
     * @param adjacentCalendar adjacent calendar
     * @return obj can as null
     */
    protected open fun getClickCalendarPaddingObject(
        x: Float,
        y: Float,
        adjacentCalendar: Calendar?
    ): Any? {
        return null
    }

    /**
     * 更新显示模式
     */
    fun updateShowMode() {
        invalidate()
    }

    /**
     * 更新周起始
     */
    fun updateWeekStart() {
        val position = tag as Int
        val calendar = CalendarUtil.getFirstCalendarStartWithMinCalendar(
            viewDelegate.minYear,
            viewDelegate.minYearMonth,
            viewDelegate.minYearDay,
            position + 1,
            viewDelegate.weekStart
        )
        setSelectedCalendar(viewDelegate.selectedCalendar)
        setup(calendar)
    }

    /**
     * 更新当选模式
     */
    fun updateSingleSelect() {
        if (!items.contains(viewDelegate.selectedCalendar)) {
            currentItem = -1
            invalidate()
        }
    }

    override fun updateCurrentDate() {
        if (items.contains(viewDelegate.currentDay)) {
            for (a in items) { //添加操作
                a.isCurrentDay = false
            }
            val index = items.indexOf(viewDelegate.currentDay)
            items[index].isCurrentDay = true
        }
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(itemHeight, MeasureSpec.EXACTLY)
        super.onMeasure(widthMeasureSpec, newHeightMeasureSpec)
    }

    /**
     * 开始绘制前的钩子，这里做一些初始化的操作，每次绘制只调用一次，性能高效
     * 没有需要可忽略不实现
     * 例如：
     * 1、需要绘制圆形标记事件背景，可以在这里计算半径
     * 2、绘制矩形选中效果，也可以在这里计算矩形宽和高
     */
    override fun onPreviewHook() {
    }

    /**
     * 循环绘制开始的回调，不需要可忽略
     * 绘制每个日历项的循环，用来计算baseLine、圆心坐标等都可以在这里实现
     *
     * @param x 日历Card x起点坐标
     */
    @Suppress("UNUSED_PARAMETER")
    protected fun onLoopStart(x: Int) {
    }

    override fun onDestroy() {}
}