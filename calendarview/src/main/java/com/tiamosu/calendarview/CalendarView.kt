package com.tiamosu.calendarview

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.tiamosu.calendarview.delegate.CalendarViewDelegate
import com.tiamosu.calendarview.entity.Calendar
import com.tiamosu.calendarview.utils.CalendarUtil
import com.tiamosu.calendarview.view.MonthViewPager
import com.tiamosu.calendarview.view.WeekBar
import com.tiamosu.calendarview.view.WeekViewPager
import com.tiamosu.calendarview.view.YearViewPager

/**
 * 日历布局
 * 各个类使用包权限，避免不必要的public
 *
 * @author tiamosu
 * @date 2020/5/25.
 */
@Suppress("unused")
open class CalendarView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    /**
     * 抽取自定义属性
     */
    private val viewDelegate = CalendarViewDelegate(context, attrs)

    /**
     * 自定义自适应高度的ViewPager，获得月视图
     */
    lateinit var monthViewPager: MonthViewPager

    /**
     * 日历周视图
     */
    lateinit var weekViewPager: WeekViewPager

    /**
     * 星期栏的线
     */
    lateinit var weekLine: View

    /**
     * 月份快速选取
     */
    lateinit var yearViewPager: YearViewPager

    /**
     * 星期栏
     */
    lateinit var weekBar: WeekBar

    /**
     * 日历外部收缩布局
     */
    var parentLayout: CalendarLayout? = null

    init {
        init(context)
    }

    /**
     * 初始化
     */
    private fun init(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.layout_calendar_view, this, true)
        val frameContent: FrameLayout = findViewById(R.id.calendarView_frameContent)
        weekViewPager = findViewById(R.id.calendarView_vpWeek)
        weekViewPager.setup(viewDelegate)

        try {
            val constructor = viewDelegate.weekBarClass.getConstructor(Context::class.java)
            weekBar = constructor.newInstance(context) as? WeekBar
                ?: throw IllegalStateException("must instanceof WeekBar")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        frameContent.addView(weekBar, 2)

        weekBar.setup(viewDelegate)
        weekBar.onWeekStartChange(viewDelegate.weekStart)

        weekLine = findViewById(R.id.calendarView_line)
        weekLine.setBackgroundColor(viewDelegate.weekLineBackground)
        val lineParams = weekLine.layoutParams as? LayoutParams
        lineParams?.setMargins(
            viewDelegate.weekLineMargin,
            viewDelegate.weekBarHeight,
            viewDelegate.weekLineMargin,
            0
        )
        weekLine.layoutParams = lineParams

        monthViewPager = findViewById(R.id.calendarView_vpMonth)
        monthViewPager.weekPager = weekViewPager
        monthViewPager.weekBar = weekBar
        val params = monthViewPager.layoutParams as? LayoutParams
        params?.setMargins(0, viewDelegate.weekBarHeight + CalendarUtil.dipToPx(context, 1f), 0, 0)
        weekViewPager.layoutParams = params

        yearViewPager = findViewById(R.id.calendarView_vpYear)
        yearViewPager.setPadding(
            viewDelegate.yearViewPaddingLeft,
            0,
            viewDelegate.yearViewPaddingRight,
            0
        )
        yearViewPager.setBackgroundColor(viewDelegate.yearViewBackground)
        yearViewPager.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                if (weekViewPager.visibility == View.VISIBLE) {
                    return
                }
                viewDelegate.yearChangeListener?.onYearChange(position + viewDelegate.minYear)
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })

        viewDelegate.innerDateSelectedListener = object : OnInnerDateSelectedListener {

            /**
             * 月视图选择事件
             * @param isClick  是否是点击
             */
            override fun onMonthDateSelected(calendar: Calendar, isClick: Boolean) {
                if (calendar.year == viewDelegate.currentDay.year
                    && calendar.month == viewDelegate.currentDay.month
                    && monthViewPager.currentItem != viewDelegate.currentMonthViewItem
                ) {
                    return
                }
                viewDelegate.indexCalendar = calendar
                if (viewDelegate.selectMode == CalendarViewDelegate.SELECT_MODE_DEFAULT || isClick) {
                    viewDelegate.selectedCalendar = calendar
                }
                weekViewPager.updateSelected(viewDelegate.indexCalendar, false)
                monthViewPager.updateSelected()
                if (viewDelegate.selectMode == CalendarViewDelegate.SELECT_MODE_DEFAULT || isClick) {
                    weekBar.onDateSelected(calendar, viewDelegate.weekStart, isClick)
                }
            }

            /**
             * 周视图选择事件
             * @param isClick 是否是点击
             */
            override fun onWeekDateSelected(calendar: Calendar, isClick: Boolean) {
                viewDelegate.indexCalendar = calendar
                if (viewDelegate.selectMode == CalendarViewDelegate.SELECT_MODE_DEFAULT || isClick
                    || calendar == viewDelegate.selectedCalendar
                ) {
                    viewDelegate.selectedCalendar = calendar
                }
                val y = calendar.year - viewDelegate.minYear
                val position = 12 * y + calendar.month - viewDelegate.minYearMonth
                weekViewPager.updateSingleSelect()
                monthViewPager.setCurrentItem(position, false)
                monthViewPager.updateSelected()
                if (viewDelegate.selectMode == CalendarViewDelegate.SELECT_MODE_DEFAULT || isClick
                    || calendar == viewDelegate.selectedCalendar
                ) {
                    weekBar.onDateSelected(calendar, viewDelegate.weekStart, isClick)
                }
            }
        }

        if (viewDelegate.selectMode == CalendarViewDelegate.SELECT_MODE_DEFAULT) {
            if (isInRange(viewDelegate.currentDay)) {
                viewDelegate.selectedCalendar = viewDelegate.createCurrentDate()
            } else {
                viewDelegate.selectedCalendar = viewDelegate.minRangeCalendar
            }
        } else {
            viewDelegate.selectedCalendar = Calendar()
        }

        viewDelegate.indexCalendar = viewDelegate.selectedCalendar
        weekBar.onDateSelected(viewDelegate.selectedCalendar, viewDelegate.weekStart, false)
        monthViewPager.setup(viewDelegate)
        monthViewPager.currentItem = viewDelegate.currentMonthViewItem
        yearViewPager.setOnMonthSelectedListener { year, month ->
            val position = 12 * (year - viewDelegate.minYear) + month - viewDelegate.minYearMonth
            closeSelectLayout(position)
            viewDelegate.isShowYearSelectedLayout = false
        }
        yearViewPager.setup(viewDelegate)
        weekViewPager.updateSelected(viewDelegate.createCurrentDate(), false)
    }

    /**
     * 设置日期范围
     *
     * @param minYear      最小年份
     * @param minYearMonth 最小年份对应月份
     * @param minYearDay   最小年份对应天
     * @param maxYear      最大月份
     * @param maxYearMonth 最大月份对应月份
     * @param maxYearDay   最大月份对应天
     */
    fun setRange(
        minYear: Int,
        minYearMonth: Int,
        minYearDay: Int,
        maxYear: Int,
        maxYearMonth: Int,
        maxYearDay: Int
    ) {
        if (CalendarUtil.compareTo(
                minYear,
                minYearMonth,
                minYearDay,
                maxYear,
                maxYearMonth,
                maxYearDay
            ) > 0
        ) {
            return
        }
        viewDelegate.setRange(
            minYear, minYearMonth, minYearDay, maxYear, maxYearMonth, maxYearDay
        )
        weekViewPager.notifyDataSetChanged()
        yearViewPager.notifyDataSetChanged()
        monthViewPager.notifyDataSetChanged()
        if (!isInRange(viewDelegate.selectedCalendar)) {
            viewDelegate.selectedCalendar = viewDelegate.minRangeCalendar
            viewDelegate.updateSelectCalendarScheme()
            viewDelegate.indexCalendar = viewDelegate.selectedCalendar
        }
        weekViewPager.updateRange()
        monthViewPager.updateRange()
        yearViewPager.updateRange()
    }

    /**
     * 打开日历年月份快速选择
     *
     * @param year 年
     */
    fun showYearSelectLayout(year: Int) {
        showSelectLayout(year)
    }

    /**
     * 打开日历年月份快速选择
     * 请使用 showYearSelectLayout(final int year) 代替，这个没什么，越来越规范
     *
     * @param year 年
     */
    private fun showSelectLayout(year: Int) {
        if (parentLayout?.contentView != null && parentLayout?.isExpand == false) {
            parentLayout?.expand()
        }
        weekViewPager.visibility = View.GONE
        viewDelegate.isShowYearSelectedLayout = true
        parentLayout?.hideContentView()

        weekBar.animate()
            .translationY(-weekBar.height.toFloat())
            .setInterpolator(LinearInterpolator())
            .setDuration(260)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    weekBar.visibility = View.GONE
                    yearViewPager.visibility = View.VISIBLE
                    yearViewPager.scrollToYear(year, false)
                    if (parentLayout?.contentView != null) {
                        parentLayout?.expand()
                    }
                }
            })

        monthViewPager.animate()
            .scaleX(0f)
            .scaleY(0f)
            .setDuration(260)
            .setInterpolator(LinearInterpolator())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    viewDelegate.yearViewChangeListener?.onYearViewChange(false)
                }
            })
    }

    /**
     * 年月份选择视图是否打开
     *
     * @return true or false
     */
    val isYearSelectLayoutVisible: Boolean
        get() = yearViewPager.visibility == View.VISIBLE

    /**
     * 关闭年月视图选择布局
     */
    fun closeYearSelectLayout() {
        if (yearViewPager.visibility == View.GONE) {
            return
        }
        val position = 12 * (viewDelegate.selectedCalendar.year - viewDelegate.minYear) +
                viewDelegate.selectedCalendar.month - viewDelegate.minYearMonth
        closeSelectLayout(position)
        viewDelegate.isShowYearSelectedLayout = false
    }

    /**
     * 关闭日历布局，同时会滚动到指定的位置
     *
     * @param position 某一年
     */
    private fun closeSelectLayout(position: Int) {
        yearViewPager.visibility = View.GONE
        weekBar.visibility = View.VISIBLE
        if (position == monthViewPager.currentItem) {
            if (viewDelegate.selectMode != CalendarViewDelegate.SELECT_MODE_SINGLE) {
                viewDelegate.calendarSelectListener?.onCalendarSelect(
                    viewDelegate.selectedCalendar,
                    false
                )
            }
        } else {
            monthViewPager.setCurrentItem(position, false)
        }

        weekBar.animate()
            .translationY(0f)
            .setInterpolator(LinearInterpolator())
            .setDuration(280)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    weekBar.visibility = View.VISIBLE
                }
            })

        monthViewPager.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(180)
            .setInterpolator(LinearInterpolator())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    viewDelegate.yearViewChangeListener?.onYearViewChange(true)

                    if (parentLayout != null) {
                        parentLayout?.showContentView()
                        if (parentLayout?.isExpand == true) {
                            monthViewPager.visibility = View.VISIBLE
                        } else {
                            weekViewPager.visibility = View.VISIBLE
                            parentLayout?.shrink()
                        }
                    } else {
                        monthViewPager.visibility = View.VISIBLE
                    }
                    monthViewPager.clearAnimation()
                }
            })
    }

    /**
     * 滚动到当前
     */
    fun scrollToCurrent(smoothScroll: Boolean = false) {
        if (!isInRange(viewDelegate.currentDay)) {
            return
        }
        val calendar = viewDelegate.createCurrentDate()
        if (viewDelegate.calendarInterceptListener?.onCalendarIntercept(calendar) == true) {
            viewDelegate.calendarInterceptListener?.onCalendarInterceptClick(calendar, false)
            return
        }
        viewDelegate.selectedCalendar = viewDelegate.createCurrentDate()
        viewDelegate.indexCalendar = viewDelegate.selectedCalendar
        viewDelegate.updateSelectCalendarScheme()
        weekBar.onDateSelected(viewDelegate.selectedCalendar, viewDelegate.weekStart, false)
        if (monthViewPager.visibility == View.VISIBLE) {
            monthViewPager.scrollToCurrent(smoothScroll)
            weekViewPager.updateSelected(viewDelegate.indexCalendar, false)
        } else {
            weekViewPager.scrollToCurrent(smoothScroll)
        }
        yearViewPager.scrollToYear(viewDelegate.currentDay.year, smoothScroll)
    }

    /**
     * 滚动到下一个月
     */
    fun scrollToNext(smoothScroll: Boolean = false) {
        when {
            isYearSelectLayoutVisible -> {
                yearViewPager.setCurrentItem(yearViewPager.currentItem + 1, smoothScroll)
            }
            weekViewPager.visibility == View.VISIBLE -> {
                weekViewPager.setCurrentItem(weekViewPager.currentItem + 1, smoothScroll)
            }
            else -> {
                monthViewPager.setCurrentItem(monthViewPager.currentItem + 1, smoothScroll)
            }
        }
    }

    /**
     * 滚动到上一个月
     */
    fun scrollToPre(smoothScroll: Boolean = false) {
        when {
            isYearSelectLayoutVisible -> {
                yearViewPager.setCurrentItem(yearViewPager.currentItem - 1, smoothScroll)
            }
            weekViewPager.visibility == View.VISIBLE -> {
                weekViewPager.setCurrentItem(weekViewPager.currentItem - 1, smoothScroll)
            }
            else -> {
                monthViewPager.setCurrentItem(monthViewPager.currentItem - 1, smoothScroll)
            }
        }
    }

    /**
     * 滚动到选择的日历
     */
    fun scrollToSelectCalendar() {
        if (!viewDelegate.selectedCalendar.isAvailable) {
            return
        }
        scrollToCalendar(
            viewDelegate.selectedCalendar.year,
            viewDelegate.selectedCalendar.month,
            viewDelegate.selectedCalendar.day,
            smoothScroll = false,
            invokeListener = true
        )
    }

    /**
     * 滚动到指定日期
     *
     * @param invokeListener 调用日期事件
     */
    fun scrollToCalendar(
        year: Int,
        month: Int,
        day: Int,
        smoothScroll: Boolean = false,
        invokeListener: Boolean = true
    ) {
        val calendar = Calendar()
        calendar.year = year
        calendar.month = month
        calendar.day = day
        if (!calendar.isAvailable) {
            return
        }
        if (!isInRange(calendar)) {
            return
        }
        if (viewDelegate.calendarInterceptListener?.onCalendarIntercept(calendar) == true) {
            viewDelegate.calendarInterceptListener?.onCalendarInterceptClick(calendar, false)
            return
        }
        if (weekViewPager.visibility == View.VISIBLE) {
            weekViewPager.scrollToCalendar(year, month, day, smoothScroll, invokeListener)
        } else {
            monthViewPager.scrollToCalendar(year, month, day, smoothScroll, invokeListener)
        }
    }

    /**
     * 滚动到某一年
     *
     * @param year 快速滚动的年份
     */
    fun scrollToYear(year: Int, smoothScroll: Boolean = false) {
        if (yearViewPager.visibility != View.VISIBLE) {
            return
        }
        yearViewPager.scrollToYear(year, smoothScroll)
    }

    /**
     * 设置月视图是否可滚动
     *
     * @param monthViewScrollable 设置月视图是否可滚动
     */
    fun setMonthViewScrollable(monthViewScrollable: Boolean) {
        viewDelegate.isMonthViewScrollable = monthViewScrollable
    }

    /**
     * 设置周视图是否可滚动
     *
     * @param weekViewScrollable 设置周视图是否可滚动
     */
    fun setWeekViewScrollable(weekViewScrollable: Boolean) {
        viewDelegate.isWeekViewScrollable = weekViewScrollable
    }

    /**
     * 设置年视图是否可滚动
     *
     * @param yearViewScrollable 设置年视图是否可滚动
     */
    fun setYearViewScrollable(yearViewScrollable: Boolean) {
        viewDelegate.isYearViewScrollable = yearViewScrollable
    }

    fun setDefaultMonthViewSelectDay() {
        viewDelegate.defaultCalendarSelectDay = CalendarViewDelegate.FIRST_DAY_OF_MONTH
    }

    fun setLastMonthViewSelectDay() {
        viewDelegate.defaultCalendarSelectDay = CalendarViewDelegate.LAST_MONTH_VIEW_SELECT_DAY
    }

    fun setLastMonthViewSelectDayIgnoreCurrent() {
        viewDelegate.defaultCalendarSelectDay =
            CalendarViewDelegate.LAST_MONTH_VIEW_SELECT_DAY_IGNORE_CURRENT
    }

    /**
     * 清除选择范围
     */
    fun clearSelectRange() {
        viewDelegate.clearSelectRange()
        monthViewPager.clearSelectRange()
        weekViewPager.clearSelectRange()
    }

    /**
     * 清除单选
     */
    fun clearSingleSelect() {
        viewDelegate.selectedCalendar = Calendar()
        monthViewPager.clearSingleSelect()
        weekViewPager.clearSingleSelect()
    }

    /**
     * 清除多选
     */
    fun clearMultiSelect() {
        viewDelegate.selectedCalendars.clear()
        monthViewPager.clearMultiSelect()
        weekViewPager.clearMultiSelect()
    }

    /**
     * 添加选择
     */
    fun putMultiSelect(vararg calendars: Calendar) {
        if (calendars.isEmpty()) {
            return
        }
        for (calendar in calendars) {
            if (viewDelegate.selectedCalendars.containsKey(calendar.toString())) {
                continue
            }
            viewDelegate.selectedCalendars[calendar.toString()] = calendar
        }
        update()
    }

    /**
     * 清楚一些多选日期
     */
    fun removeMultiSelect(vararg calendars: Calendar) {
        if (calendars.isEmpty()) {
            return
        }
        for (calendar in calendars) {
            if (viewDelegate.selectedCalendars.containsKey(calendar.toString())) {
                viewDelegate.selectedCalendars.remove(calendar.toString())
            }
        }
        update()
    }

    val multiSelectCalendars: List<Calendar>
        get() {
            val calendars: MutableList<Calendar> = ArrayList()
            if (viewDelegate.selectedCalendars.isEmpty()) {
                return calendars
            }
            calendars.addAll(viewDelegate.selectedCalendars.values)
            calendars.sort()
            return calendars
        }

    /**
     * 获取选中范围
     */
    val selectCalendarRange: List<Calendar>?
        get() = viewDelegate.selectCalendarRange

    /**
     * 设置月视图项高度
     *
     * @param calendarItemHeight MonthView item height
     */
    fun setCalendarItemHeight(calendarItemHeight: Int) {
        if (viewDelegate.calendarItemHeight == calendarItemHeight) {
            return
        }
        viewDelegate.calendarItemHeight = calendarItemHeight
        monthViewPager.updateItemHeight()
        weekViewPager.updateItemHeight()
        parentLayout?.updateCalendarItemHeight()
    }

    /**
     * 设置月视图
     *
     * @param cls MonthView.class
     */
    fun setMonthView(cls: Class<*>) {
        if (viewDelegate.monthViewClass == cls) {
            return
        }
        viewDelegate.monthViewClass = cls
        monthViewPager.updateMonthViewClass()
    }

    /**
     * 设置周视图
     *
     * @param cls WeekView.class
     */
    fun setWeekView(cls: Class<*>) {
        if (viewDelegate.weekBarClass == cls) {
            return
        }
        viewDelegate.weekViewClass = cls
        weekViewPager.updateWeekViewClass()
    }

    /**
     * 设置周栏视图
     *
     * @param cls WeekBar.class
     */
    fun setWeekBar(cls: Class<*>) {
        if (viewDelegate.weekBarClass == cls) {
            return
        }
        viewDelegate.weekBarClass = cls
        val frameContent: FrameLayout = findViewById(R.id.calendarView_frameContent)
        frameContent.removeView(weekBar)
        try {
            val constructor = cls.getConstructor(Context::class.java)
            weekBar = constructor.newInstance(context) as? WeekBar
                ?: throw IllegalStateException("must instanceof WeekBar")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        frameContent.addView(weekBar, 2)
        weekBar.setup(viewDelegate)
        weekBar.onWeekStartChange(viewDelegate.weekStart)
        monthViewPager.weekBar = weekBar
        weekBar.onDateSelected(viewDelegate.selectedCalendar, viewDelegate.weekStart, false)
    }

    /**
     * 添加日期拦截事件
     * 使用此方法，只能基于select_mode = single_mode
     * 否则的话，如果标记全部日期为不可点击，那是没有意义的，
     * 框架本身也不可能在滑动的过程中全部去判断每个日期的可点击性
     */
    fun setOnCalendarInterceptListener(listener: OnCalendarInterceptListener?) {
        if (listener == null) {
            viewDelegate.calendarInterceptListener = null
        }
        if (listener == null || viewDelegate.selectMode == CalendarViewDelegate.SELECT_MODE_DEFAULT) {
            return
        }
        viewDelegate.calendarInterceptListener = listener
        if (!listener.onCalendarIntercept(viewDelegate.selectedCalendar)) {
            return
        }
        viewDelegate.selectedCalendar = Calendar()
    }

    /**
     * 点击视图Padding位置的事件
     *
     * @param listener listener
     */
    fun setOnClickCalendarPaddingListener(listener: OnClickCalendarPaddingListener?) {
        viewDelegate.clickCalendarPaddingListener = listener
    }

    /**
     * 年份改变事件
     */
    fun setOnYearChangeListener(listener: OnYearChangeListener?) {
        viewDelegate.yearChangeListener = listener
    }

    /**
     * 月份改变事件
     */
    fun setOnMonthChangeListener(listener: OnMonthChangeListener?) {
        viewDelegate.monthChangeListener = listener
    }

    /**
     * 周视图切换监听
     */
    fun setOnWeekChangeListener(listener: OnWeekChangeListener?) {
        viewDelegate.weekChangeListener = listener
    }

    /**
     * 日期选择事件
     */
    fun setOnCalendarSelectListener(listener: OnCalendarSelectListener?) {
        viewDelegate.calendarSelectListener = listener
        if (viewDelegate.calendarSelectListener == null) {
            return
        }
        if (viewDelegate.selectMode != CalendarViewDelegate.SELECT_MODE_DEFAULT) {
            return
        }
        if (!isInRange(viewDelegate.selectedCalendar)) {
            return
        }
        viewDelegate.updateSelectCalendarScheme()
    }

    /**
     * 日期选择事件
     */
    fun setOnCalendarRangeSelectListener(listener: OnCalendarRangeSelectListener?) {
        viewDelegate.calendarRangeSelectListener = listener
    }

    /**
     * 日期多选事件
     */
    fun setOnCalendarMultiSelectListener(listener: OnCalendarMultiSelectListener?) {
        viewDelegate.calendarMultiSelectListener = listener
    }

    /**
     * 设置最小范围和最大访问，default：minRange = -1，maxRange = -1 没有限制
     */
    fun setSelectRange(minRange: Int, maxRange: Int) {
        if (minRange > maxRange) {
            return
        }
        viewDelegate.setSelectRange(minRange, maxRange)
    }

    fun setSelectStartCalendar(startYear: Int, startMonth: Int, startDay: Int) {
        if (viewDelegate.selectMode != CalendarViewDelegate.SELECT_MODE_RANGE) {
            return
        }
        val startCalendar = Calendar()
        startCalendar.year = startYear
        startCalendar.month = startMonth
        startCalendar.day = startDay
        setSelectStartCalendar(startCalendar)
    }

    fun setSelectStartCalendar(startCalendar: Calendar?) {
        if (viewDelegate.selectMode != CalendarViewDelegate.SELECT_MODE_RANGE || startCalendar == null) {
            return
        }
        if (!isInRange(startCalendar)) {
            viewDelegate.calendarRangeSelectListener?.onSelectOutOfRange(startCalendar, true)
            return
        }
        if (onCalendarIntercept(startCalendar)) {
            viewDelegate.calendarInterceptListener?.onCalendarInterceptClick(startCalendar, false)
            return
        }
        viewDelegate.selectedEndRangeCalendar = null
        viewDelegate.selectedStartRangeCalendar = startCalendar
        scrollToCalendar(startCalendar.year, startCalendar.month, startCalendar.day)
    }

    fun setSelectEndCalendar(endYear: Int, endMonth: Int, endDay: Int) {
        if (viewDelegate.selectMode != CalendarViewDelegate.SELECT_MODE_RANGE) {
            return
        }
        if (viewDelegate.selectedStartRangeCalendar == null) {
            return
        }
        val endCalendar = Calendar().apply {
            year = endYear
            month = endMonth
            day = endDay
        }
        setSelectEndCalendar(endCalendar)
    }

    fun setSelectEndCalendar(endCalendar: Calendar?) {
        if (viewDelegate.selectMode != CalendarViewDelegate.SELECT_MODE_RANGE) {
            return
        }
        if (viewDelegate.selectedStartRangeCalendar == null) {
            return
        }
        setSelectCalendarRange(viewDelegate.selectedStartRangeCalendar, endCalendar)
    }

    /**
     * 直接指定选择范围，set select calendar range
     */
    fun setSelectCalendarRange(
        startYear: Int, startMonth: Int, startDay: Int,
        endYear: Int, endMonth: Int, endDay: Int
    ) {
        if (viewDelegate.selectMode != CalendarViewDelegate.SELECT_MODE_RANGE) {
            return
        }
        val startCalendar = Calendar().apply {
            year = startYear
            month = startMonth
            day = startDay
        }
        val endCalendar = Calendar().apply {
            year = endYear
            month = endMonth
            day = endDay
        }
        setSelectCalendarRange(startCalendar, endCalendar)
    }

    /**
     * 设置选择日期范围
     */
    fun setSelectCalendarRange(startCalendar: Calendar?, endCalendar: Calendar?) {
        if (viewDelegate.selectMode != CalendarViewDelegate.SELECT_MODE_RANGE) {
            return
        }
        if (startCalendar == null || endCalendar == null) {
            return
        }
        if (onCalendarIntercept(startCalendar)) {
            viewDelegate.calendarInterceptListener?.onCalendarInterceptClick(startCalendar, false)
            return
        }
        if (onCalendarIntercept(endCalendar)) {
            viewDelegate.calendarInterceptListener?.onCalendarInterceptClick(endCalendar, false)
            return
        }
        val minDiffer = endCalendar.differ(startCalendar)
        if (minDiffer < 0) {
            return
        }
        if (!isInRange(startCalendar) || !isInRange(endCalendar)) {
            return
        }

        //优先判断各种直接return的情况，减少代码深度
        if (viewDelegate.minSelectRange != -1 && viewDelegate.minSelectRange > minDiffer + 1) {
            viewDelegate.calendarRangeSelectListener?.onSelectOutOfRange(endCalendar, true)
            return
        } else if (viewDelegate.maxSelectRange != -1 && viewDelegate.maxSelectRange < minDiffer + 1) {
            viewDelegate.calendarRangeSelectListener?.onSelectOutOfRange(endCalendar, false)
            return
        }
        if (viewDelegate.minSelectRange == -1 && minDiffer == 0) {
            viewDelegate.selectedStartRangeCalendar = startCalendar
            viewDelegate.selectedEndRangeCalendar = null
            viewDelegate.calendarRangeSelectListener?.onCalendarRangeSelect(startCalendar, false)
            scrollToCalendar(startCalendar.year, startCalendar.month, startCalendar.day)
            return
        }
        viewDelegate.selectedStartRangeCalendar = startCalendar
        viewDelegate.selectedEndRangeCalendar = endCalendar
        viewDelegate.calendarRangeSelectListener?.onCalendarRangeSelect(startCalendar, false)
        viewDelegate.calendarRangeSelectListener?.onCalendarRangeSelect(endCalendar, true)
        scrollToCalendar(startCalendar.year, startCalendar.month, startCalendar.day)
    }

    /**
     * 是否拦截日期，此设置续设置mCalendarInterceptListener
     *
     * @return 是否拦截日期
     */
    protected fun onCalendarIntercept(calendar: Calendar): Boolean {
        return viewDelegate.calendarInterceptListener?.onCalendarIntercept(calendar) == true
    }

    /**
     * 设置最大多选数量，获得最大多选数量
     */
    var maxMultiSelectSize: Int
        get() = viewDelegate.maxMultiSelectSize
        set(maxMultiSelectSize) {
            viewDelegate.maxMultiSelectSize = maxMultiSelectSize
        }

    /**
     * 最小选择范围
     *
     * @return 最小选择范围
     */
    val minSelectRange: Int
        get() = viewDelegate.minSelectRange

    /**
     * 最大选择范围
     *
     * @return 最大选择范围
     */
    val maxSelectRange: Int
        get() = viewDelegate.maxSelectRange

    /**
     * 日期长按事件
     */
    fun setOnCalendarLongClickListener(listener: OnCalendarLongClickListener?) {
        viewDelegate.calendarLongClickListener = listener
    }

    /**
     * 日期长按事件
     *
     * @param preventLongPressedSelect 防止长按选择日期
     */
    fun setOnCalendarLongClickListener(
        listener: OnCalendarLongClickListener?,
        preventLongPressedSelect: Boolean
    ) {
        viewDelegate.calendarLongClickListener = listener
        viewDelegate.isPreventLongPressedSelected = preventLongPressedSelect
    }

    /**
     * 视图改变事件
     */
    fun setOnViewChangeListener(listener: OnViewChangeListener?) {
        viewDelegate.viewChangeListener = listener
    }

    fun setOnYearViewChangeListener(listener: OnYearViewChangeListener?) {
        viewDelegate.yearViewChangeListener = listener
    }

    /**
     * 保持状态
     */
    override fun onSaveInstanceState(): Parcelable? {
        return Bundle().apply {
            val parcelable = super.onSaveInstanceState()
            putParcelable("super", parcelable)
            putSerializable("selected_calendar", viewDelegate.selectedCalendar)
            putSerializable("index_calendar", viewDelegate.indexCalendar)
        }
    }

    /**
     * 恢复状态
     */
    override fun onRestoreInstanceState(state: Parcelable?) {
        (state as? Bundle)?.apply {
            val superData = getParcelable<Parcelable>("super")
            viewDelegate.selectedCalendar = getSerializable("selected_calendar") as Calendar
            viewDelegate.indexCalendar = getSerializable("index_calendar") as Calendar
            viewDelegate.calendarSelectListener?.onCalendarSelect(
                viewDelegate.selectedCalendar,
                false
            )
            scrollToCalendar(
                viewDelegate.indexCalendar.year,
                viewDelegate.indexCalendar.month,
                viewDelegate.indexCalendar.day
            )
            update()
            super.onRestoreInstanceState(superData)
        }
    }

    /**
     * 初始化时初始化日历卡默认选择位置
     */
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        (parent as? CalendarLayout)?.also {
            parentLayout = it
            monthViewPager.parentLayout = it
            weekViewPager.parentLayout = it
            it.weekBar = weekBar
            it.setup(viewDelegate)
            it.initStatus()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val height = MeasureSpec.getSize(heightMeasureSpec)
        if (!viewDelegate.isFullScreenCalendar || height == 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }
        setCalendarItemHeight((height - viewDelegate.weekBarHeight) / 6)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    /**
     * 标记哪些日期有事件
     *
     * @param schemeDates mSchemeDatesMap 通过自己的需求转换即可
     */
    fun setSchemeDate(schemeDates: MutableMap<String, Calendar>) {
        viewDelegate.schemeDatesMap = schemeDates
        viewDelegate.updateSelectCalendarScheme()
        yearViewPager.update()
        monthViewPager.updateScheme()
        weekViewPager.updateScheme()
    }

    /**
     * 清空日期标记
     */
    fun clearSchemeDate() {
        viewDelegate.schemeDatesMap.clear()
        viewDelegate.clearSelectedScheme()
        yearViewPager.update()
        monthViewPager.updateScheme()
        weekViewPager.updateScheme()
    }

    /**
     * 添加事物标记
     */
    fun addSchemeDate(calendar: Calendar) {
        if (!calendar.isAvailable) {
            return
        }
        viewDelegate.schemeDatesMap.remove(calendar.toString())
        viewDelegate.schemeDatesMap[calendar.toString()] = calendar
        viewDelegate.updateSelectCalendarScheme()
        yearViewPager.update()
        monthViewPager.updateScheme()
        weekViewPager.updateScheme()
    }

    /**
     * 添加事物标记
     */
    fun addSchemeDate(schemeDates: Map<String, Calendar>) {
        if (schemeDates.isEmpty()) {
            return
        }
        viewDelegate.addSchemes(schemeDates)
        viewDelegate.updateSelectCalendarScheme()
        yearViewPager.update()
        monthViewPager.updateScheme()
        weekViewPager.updateScheme()
    }

    /**
     * 移除某天的标记
     * 这个API是安全的
     *
     * @param calendar calendar
     */
    fun removeSchemeDate(calendar: Calendar) {
        if (viewDelegate.schemeDatesMap.isEmpty()) {
            return
        }
        viewDelegate.schemeDatesMap.remove(calendar.toString())
        if (viewDelegate.selectedCalendar == calendar) {
            viewDelegate.clearSelectedScheme()
        }
        yearViewPager.update()
        monthViewPager.updateScheme()
        weekViewPager.updateScheme()
    }

    /**
     * 设置背景色
     *
     * @param yearViewBackground 年份卡片的背景色
     * @param weekBackground     星期栏背景色
     * @param lineBg             线的颜色
     */
    fun setBackground(yearViewBackground: Int, weekBackground: Int, lineBg: Int) {
        weekBar.setBackgroundColor(weekBackground)
        yearViewPager.setBackgroundColor(yearViewBackground)
        weekLine.setBackgroundColor(lineBg)
    }

    /**
     * 设置文本颜色
     *
     * @param currentDayTextColor      今天字体颜色
     * @param curMonthTextColor        当前月份字体颜色
     * @param otherMonthColor          其它月份字体颜色
     * @param curMonthLunarTextColor   当前月份农历字体颜色
     * @param otherMonthLunarTextColor 其它农历字体颜色
     */
    fun setTextColor(
        currentDayTextColor: Int,
        curMonthTextColor: Int,
        otherMonthColor: Int,
        curMonthLunarTextColor: Int,
        otherMonthLunarTextColor: Int
    ) {
        viewDelegate.setTextColor(
            currentDayTextColor, curMonthTextColor,
            otherMonthColor, curMonthLunarTextColor, otherMonthLunarTextColor
        )
        monthViewPager.updateStyle()
        weekViewPager.updateStyle()
    }

    /**
     * 设置选择的效果
     *
     * @param selectedThemeColor     选中的标记颜色
     * @param selectedTextColor      选中的字体颜色
     * @param selectedLunarTextColor 选中的农历字体颜色
     */
    fun setSelectedColor(
        selectedThemeColor: Int,
        selectedTextColor: Int,
        selectedLunarTextColor: Int
    ) {
        viewDelegate.setSelectColor(selectedThemeColor, selectedTextColor, selectedLunarTextColor)
        monthViewPager.updateStyle()
        weekViewPager.updateStyle()
    }

    /**
     * 定制颜色
     *
     * @param selectedThemeColor 选中的标记颜色
     * @param schemeColor        标记背景色
     */
    fun setThemeColor(selectedThemeColor: Int, schemeColor: Int) {
        viewDelegate.setThemeColor(selectedThemeColor, schemeColor)
        monthViewPager.updateStyle()
        weekViewPager.updateStyle()
    }

    /**
     * 设置标记的色
     *
     * @param schemeLunarTextColor 标记农历颜色
     * @param schemeColor          标记背景色
     * @param schemeTextColor      标记字体颜色
     */
    fun setSchemeColor(schemeColor: Int, schemeTextColor: Int, schemeLunarTextColor: Int) {
        viewDelegate.setSchemeColor(schemeColor, schemeTextColor, schemeLunarTextColor)
        monthViewPager.updateStyle()
        weekViewPager.updateStyle()
    }

    /**
     * 设置年视图的颜色
     *
     * @param yearViewMonthTextColor 年视图月份颜色
     * @param yearViewDayTextColor   年视图天的颜色
     * @param yarViewSchemeTextColor 年视图标记颜色
     */
    fun setYearViewTextColor(
        yearViewMonthTextColor: Int,
        yearViewDayTextColor: Int,
        yarViewSchemeTextColor: Int
    ) {
        viewDelegate.setYearViewTextColor(
            yearViewMonthTextColor,
            yearViewDayTextColor,
            yarViewSchemeTextColor
        )
        yearViewPager.updateStyle()
    }

    /**
     * 设置星期栏的背景和字体颜色
     *
     * @param weekBackground 背景色
     * @param weekTextColor  字体颜色
     */
    fun setWeeColor(weekBackground: Int, weekTextColor: Int) {
        weekBar.setBackgroundColor(weekBackground)
        weekBar.setTextColor(weekTextColor)
    }

    fun setCalendarPadding(calendarPadding: Int) {
        viewDelegate.setCalendarPadding(calendarPadding)
        update()
    }

    fun setCalendarPaddingLeft(calendarPaddingLeft: Int) {
        viewDelegate.setCalendarPaddingLeft(calendarPaddingLeft)
        update()
    }

    fun setCalendarPaddingRight(calendarPaddingRight: Int) {
        viewDelegate.setCalendarPaddingRight(calendarPaddingRight)
        update()
    }

    /**
     * 默认选择模式
     */
    fun setSelectDefaultMode() {
        if (viewDelegate.selectMode == CalendarViewDelegate.SELECT_MODE_DEFAULT) {
            return
        }
        viewDelegate.selectedCalendar = viewDelegate.indexCalendar
        viewDelegate.selectMode = CalendarViewDelegate.SELECT_MODE_DEFAULT
        weekBar.onDateSelected(viewDelegate.selectedCalendar, viewDelegate.weekStart, false)
        monthViewPager.updateDefaultSelect()
        weekViewPager.updateDefaultSelect()
    }

    /**
     * 范围模式
     */
    fun setSelectRangeMode() {
        if (viewDelegate.selectMode == CalendarViewDelegate.SELECT_MODE_RANGE) {
            return
        }
        viewDelegate.selectMode = CalendarViewDelegate.SELECT_MODE_RANGE
        clearSelectRange()
    }

    /**
     * 多选模式
     */
    fun setSelectMultiMode() {
        if (viewDelegate.selectMode == CalendarViewDelegate.SELECT_MODE_MULTI) {
            return
        }
        viewDelegate.selectMode = CalendarViewDelegate.SELECT_MODE_MULTI
        clearMultiSelect()
    }

    /**
     * 单选模式
     */
    fun setSelectSingleMode() {
        if (viewDelegate.selectMode == CalendarViewDelegate.SELECT_MODE_SINGLE) {
            return
        }
        viewDelegate.selectMode = CalendarViewDelegate.SELECT_MODE_SINGLE
        weekViewPager.updateSelected()
        monthViewPager.updateSelected()
    }

    /**
     * 设置星期日周起始
     */
    fun setWeekStarWithSun() {
        setWeekStart(CalendarViewDelegate.WEEK_START_WITH_SUN)
    }

    /**
     * 设置星期一周起始
     */
    fun setWeekStarWithMon() {
        setWeekStart(CalendarViewDelegate.WEEK_START_WITH_MON)
    }

    /**
     * 设置星期六周起始
     */
    fun setWeekStarWithSat() {
        setWeekStart(CalendarViewDelegate.WEEK_START_WITH_SAT)
    }

    /**
     * 设置周起始
     * CalendarViewDelegate.WEEK_START_WITH_SUN
     * CalendarViewDelegate.WEEK_START_WITH_MON
     * CalendarViewDelegate.WEEK_START_WITH_SAT
     *
     * @param weekStart 周起始
     */
    private fun setWeekStart(weekStart: Int) {
        if (weekStart != CalendarViewDelegate.WEEK_START_WITH_SUN
            && weekStart != CalendarViewDelegate.WEEK_START_WITH_MON
            && weekStart != CalendarViewDelegate.WEEK_START_WITH_SAT
        ) {
            return
        }
        if (weekStart == viewDelegate.weekStart) {
            return
        }
        viewDelegate.weekStart = weekStart
        weekBar.onWeekStartChange(weekStart)
        weekBar.onDateSelected(viewDelegate.selectedCalendar, weekStart, false)
        weekViewPager.updateWeekStart()
        monthViewPager.updateWeekStart()
        yearViewPager.updateWeekStart()
    }

    /**
     * 是否是单选模式
     */
    val isSingleSelectMode: Boolean
        get() = viewDelegate.selectMode == CalendarViewDelegate.SELECT_MODE_SINGLE

    /**
     * 设置显示模式为全部
     */
    fun setAllMode() {
        setShowMode(CalendarViewDelegate.MODE_ALL_MONTH)
    }

    /**
     * 设置显示模式为仅当前月份
     */
    fun setOnlyCurrentMode() {
        setShowMode(CalendarViewDelegate.MODE_ONLY_CURRENT_MONTH)
    }

    /**
     * 设置显示模式为填充
     */
    fun setFixMode() {
        setShowMode(CalendarViewDelegate.MODE_FIT_MONTH)
    }

    /**
     * 设置显示模式
     * CalendarViewDelegate.MODE_ALL_MONTH
     * CalendarViewDelegate.MODE_ONLY_CURRENT_MONTH
     * CalendarViewDelegate.MODE_FIT_MONTH
     *
     * @param mode 月视图显示模式
     */
    private fun setShowMode(mode: Int) {
        if (mode != CalendarViewDelegate.MODE_ALL_MONTH
            && mode != CalendarViewDelegate.MODE_ONLY_CURRENT_MONTH
            && mode != CalendarViewDelegate.MODE_FIT_MONTH
        ) {
            return
        }
        if (viewDelegate.monthViewShowMode == mode) {
            return
        }
        viewDelegate.monthViewShowMode = mode
        weekViewPager.updateShowMode()
        monthViewPager.updateShowMode()
        weekViewPager.notifyDataSetChanged()
    }

    /**
     * 更新界面，
     * 重新设置颜色等都需要调用该方法
     */
    fun update() {
        weekBar.onWeekStartChange(viewDelegate.weekStart)
        yearViewPager.update()
        monthViewPager.updateScheme()
        weekViewPager.updateScheme()
    }

    /**
     * 更新周视图
     */
    fun updateWeekBar() {
        weekBar.onWeekStartChange(viewDelegate.weekStart)
    }

    /**
     * 更新当前日期
     */
    fun updateCurrentDate() {
        val calendar = java.util.Calendar.getInstance()
        val day = calendar[java.util.Calendar.DAY_OF_MONTH]
        if (curDay == day) {
            return
        }
        viewDelegate.updateCurrentDay()
        monthViewPager.updateCurrentDate()
        weekViewPager.updateCurrentDate()
    }

    /**
     * 获取当天
     *
     * @return 返回今天
     */
    val curDay: Int
        get() = viewDelegate.currentDay.day

    /**
     * 获取本月
     *
     * @return 返回本月
     */
    val curMonth: Int
        get() = viewDelegate.currentDay.month

    /**
     * 获取本年
     *
     * @return 返回本年
     */
    val curYear: Int
        get() = viewDelegate.currentDay.year

    /**
     * 获取当天日历对象
     */
    val curCalendar: Calendar
        get() = viewDelegate.currentDay

    /**
     * 获取当前周数据
     *
     * @return 获取当前周数据
     */
    val currentWeekCalendars: List<Calendar>
        get() = weekViewPager.currentWeekCalendars

    /**
     * 获取当前月份日期
     */
    val currentMonthCalendars: List<Calendar>?
        get() = monthViewPager.currentMonthCalendars

    /**
     * 获取选择的日期
     *
     * @return 获取选择的日期
     */
    val selectedCalendar: Calendar
        get() = viewDelegate.selectedCalendar

    /**
     * 获得最小范围日期
     *
     * @return 最小范围日期
     */
    val minRangeCalendar: Calendar
        get() = viewDelegate.minRangeCalendar

    /**
     * 获得最大范围日期
     *
     * @return 最大范围日期
     */
    val maxRangeCalendar: Calendar
        get() = viewDelegate.maxRangeCalendar

    /**
     * 是否在日期范围内
     *
     * @return 是否在日期范围内
     */
    protected fun isInRange(calendar: Calendar): Boolean {
        return CalendarUtil.isCalendarInRange(calendar, viewDelegate)
    }

    /**
     * 年份视图切换事件，快速年份切换
     */
    fun interface OnYearChangeListener {
        fun onYearChange(year: Int)
    }

    /**
     * 月份切换事件
     */
    fun interface OnMonthChangeListener {
        fun onMonthChange(year: Int, month: Int)
    }

    /**
     * 周视图切换事件
     */
    fun interface OnWeekChangeListener {
        fun onWeekChange(weekCalendars: List<Calendar>)
    }

    /**
     * 内部日期选择，不暴露外部使用
     * 主要是用于更新日历CalendarLayout位置
     */
    interface OnInnerDateSelectedListener {
        /**
         * 月视图点击
         *
         * @param isClick  是否是点击
         */
        fun onMonthDateSelected(calendar: Calendar, isClick: Boolean)

        /**
         * 周视图点击
         *
         * @param isClick  是否是点击
         */
        fun onWeekDateSelected(calendar: Calendar, isClick: Boolean)
    }

    /**
     * 日历范围选择事件
     */
    interface OnCalendarRangeSelectListener {

        /**
         * 范围选择超出范围越界
         */
        fun onCalendarSelectOutOfRange(calendar: Calendar)

        /**
         * 选择范围超出范围
         *
         * @param isOutOfMinRange 是否小于最小范围，否则为最大范围
         */
        fun onSelectOutOfRange(calendar: Calendar, isOutOfMinRange: Boolean)

        /**
         * 日期选择事件
         *
         * @param isEnd    是否结束
         */
        fun onCalendarRangeSelect(calendar: Calendar, isEnd: Boolean)
    }

    /**
     * 日历多选事件
     */
    interface OnCalendarMultiSelectListener {

        /**
         * 多选超出范围越界
         */
        fun onCalendarMultiSelectOutOfRange(calendar: Calendar)

        /**
         * 多选超出大小
         *
         * @param maxSize  最大大小
         */
        fun onMultiSelectOutOfSize(calendar: Calendar, maxSize: Int)

        /**
         * 多选事件
         */
        fun onCalendarMultiSelect(calendar: Calendar, curSize: Int, maxSize: Int)
    }

    /**
     * 日历选择事件
     */
    interface OnCalendarSelectListener {

        /**
         * 超出范围越界
         */
        fun onCalendarOutOfRange(calendar: Calendar)

        /**
         * 日期选择事件
         */
        fun onCalendarSelect(calendar: Calendar, isClick: Boolean)
    }

    interface OnCalendarLongClickListener {

        /**
         * 超出范围越界
         */
        fun onCalendarLongClickOutOfRange(calendar: Calendar)

        /**
         * 日期长按事件
         */
        fun onCalendarLongClick(calendar: Calendar)
    }

    /**
     * 视图改变事件
     */
    fun interface OnViewChangeListener {

        /**
         * 视图改变事件
         *
         * @param isMonthView isMonthView是否是月视图
         */
        fun onViewChange(isMonthView: Boolean)
    }

    /**
     * 年视图改变事件
     */
    fun interface OnYearViewChangeListener {

        /**
         * 年视图变化
         *
         * @param isClose 是否关闭
         */
        fun onYearViewChange(isClose: Boolean)
    }

    /**
     * 拦截日期是否可用事件
     */
    interface OnCalendarInterceptListener {
        fun onCalendarIntercept(calendar: Calendar): Boolean
        fun onCalendarInterceptClick(calendar: Calendar, isClick: Boolean)
    }

    /**
     * 点击Padding位置事件
     */
    fun interface OnClickCalendarPaddingListener {
        /**
         * 点击Padding位置的事件
         *
         * @param x                x坐标
         * @param y                y坐标
         * @param isMonthView      是否是月视图，不是则为周视图
         * @param adjacentCalendar 相邻的日历日期
         * @param obj              此处的对象，自行设置
         */
        fun onClickCalendarPadding(
            x: Float,
            y: Float,
            isMonthView: Boolean,
            adjacentCalendar: Calendar?,
            obj: Any?
        )
    }
}