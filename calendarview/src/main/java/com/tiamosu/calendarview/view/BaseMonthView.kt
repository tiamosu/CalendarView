package com.tiamosu.calendarview.view

import android.content.Context
import com.tiamosu.calendarview.delegate.CalendarViewDelegate
import com.tiamosu.calendarview.entity.Calendar
import com.tiamosu.calendarview.utils.CalendarUtil

/**
 * 月视图基础控件,可自由继承实现
 * 可通过此扩展各种视图如：MonthView、RangeMonthView、MultiMonthView
 *
 * @author tiamosu
 * @date 2020/5/24.
 */
abstract class BaseMonthView(context: Context) : BaseView(context) {

    lateinit var monthViewPager: MonthViewPager

    /**
     * 当前日历卡年份
     */
    protected var year = 0

    /**
     * 当前日历卡月份
     */
    protected var month = 0

    /**
     * 日历的行数
     */
    protected var lineCount = 0

    /**
     * 日历高度
     */
    protected var monthHeight = 0

    /**
     * 下个月偏移的数量
     */
    protected var nextDiff = 0

    /**
     * 初始化日期
     */
    fun initMonthWithDate(year: Int, month: Int) {
        this.year = year
        this.month = month
        initCalendar()
        monthHeight = CalendarUtil.getMonthViewHeight(
            year, month,
            itemHeight, viewDelegate.weekStart, viewDelegate.monthViewShowMode
        )
    }

    /**
     * 初始化日历
     */
    private fun initCalendar() {
        nextDiff = CalendarUtil.getMonthEndDiff(year, month, viewDelegate.weekStart)
        val preDiff = CalendarUtil.getMonthViewStartDiff(year, month, viewDelegate.weekStart)
        val monthDayCount = CalendarUtil.getMonthDaysCount(year, month)
        items = CalendarUtil.initCalendarForMonthView(
            year,
            month,
            viewDelegate.currentDay,
            viewDelegate.weekStart
        )

        currentItem = if (items.contains(viewDelegate.currentDay)) {
            items.indexOf(viewDelegate.currentDay)
        } else {
            items.indexOf(viewDelegate.selectedCalendar)
        }
        if (currentItem > 0 && viewDelegate.calendarInterceptListener
                ?.onCalendarIntercept(viewDelegate.selectedCalendar) == true
        ) {
            currentItem = -1
        }
        lineCount = if (viewDelegate.monthViewShowMode == CalendarViewDelegate.MODE_ALL_MONTH) {
            6
        } else {
            (preDiff + monthDayCount + nextDiff) / 7
        }
        addSchemesFromMap()
        invalidate()
    }

    /**
     * 获取点击选中的日期
     */
    protected open fun getIndex(): Calendar? {
        if (itemWidth == 0 || itemHeight == 0) {
            return null
        }
        if (mX <= viewDelegate.calendarPaddingLeft || mX >= width - viewDelegate.calendarPaddingRight) {
            return null
        }
        var indexX = (mX - viewDelegate.calendarPaddingLeft).toInt() / itemWidth
        if (indexX >= 7) {
            indexX = 6
        }
        val indexY = mY.toInt() / itemHeight
        val position = indexY * 7 + indexX // 选择项
        return if (position >= 0 && position < items.size) items[position] else null
    }

    /**
     * 记录已经选择的日期
     *
     * @param calendar calendar
     */
    fun setSelectedCalendar(calendar: Calendar) {
        currentItem = items.indexOf(calendar)
    }

    /**
     * 更新显示模式
     */
    fun updateShowMode() {
        lineCount = CalendarUtil.getMonthViewLineCount(
            year, month,
            viewDelegate.weekStart, viewDelegate.monthViewShowMode
        )
        monthHeight = CalendarUtil.getMonthViewHeight(
            year, month,
            itemHeight, viewDelegate.weekStart, viewDelegate.monthViewShowMode
        )
        invalidate()
    }

    /**
     * 更新周起始
     */
    fun updateWeekStart() {
        initCalendar()
        monthHeight = CalendarUtil.getMonthViewHeight(
            year, month,
            itemHeight, viewDelegate.weekStart, viewDelegate.monthViewShowMode
        )
    }

    override fun updateItemHeight() {
        super.updateItemHeight()
        monthHeight = CalendarUtil.getMonthViewHeight(
            year, month,
            itemHeight, viewDelegate.weekStart, viewDelegate.monthViewShowMode
        )
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

    /**
     * 获取选中的下标
     *
     * @param calendar calendar
     * @return 获取选中的下标
     */
    fun getSelectedIndex(calendar: Calendar?): Int {
        return items.indexOf(calendar)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var newHeightMeasureSpec = heightMeasureSpec
        if (lineCount != 0) {
            newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(monthHeight, MeasureSpec.EXACTLY)
        }
        super.onMeasure(widthMeasureSpec, newHeightMeasureSpec)
    }

    /**
     * 开始绘制前的钩子，这里做一些初始化的操作，每次绘制只调用一次，性能高效
     * 没有需要可忽略不实现
     * 例如：
     * 1、需要绘制圆形标记事件背景，可以在这里计算半径
     * 2、绘制矩形选中效果，也可以在这里计算矩形宽和高
     */
    override fun onPreviewHook() {}

    /**
     * 循环绘制开始的回调，不需要可忽略
     * 绘制每个日历项的循环，用来计算baseLine、圆心坐标等都可以在这里实现
     *
     * @param x 日历Card x起点坐标
     * @param y 日历Card y起点坐标
     */
    protected open fun onLoopStart(x: Int, y: Int) {}

    override fun onDestroy() {}
}