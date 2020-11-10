package com.tiamosu.calendarview.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.tiamosu.calendarview.CalendarLayout
import com.tiamosu.calendarview.delegate.CalendarViewDelegate
import com.tiamosu.calendarview.entity.Calendar
import com.tiamosu.calendarview.utils.CalendarUtil
import com.tiamosu.calendarview.utils.LunarCalendar

/**
 * 周视图滑动ViewPager，需要动态固定高度
 * 周视图是连续不断的视图，因此不能简单的得出每年都有52+1周，这样会计算重叠的部分
 * WeekViewPager 需要和 CalendarView 关联:
 *
 * @author tiamosu
 * @date 2020/5/25.
 */
class WeekViewPager @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ViewPager(context, attrs) {

    private var isUpdateWeekView = false
    private var weekCount = 0
    private lateinit var viewDelegate: CalendarViewDelegate

    /**
     * 日历布局，需要在日历下方放自己的布局
     */
    lateinit var parentLayout: CalendarLayout

    /**
     * 是否使用滚动到某一天
     */
    private var isUsingScrollToCalendar = false

    fun setup(delegate: CalendarViewDelegate) {
        viewDelegate = delegate
        init()
    }

    private fun init() {
        weekCount = CalendarUtil.getWeekCountBetweenBothCalendar(
            viewDelegate.minYear,
            viewDelegate.minYearMonth,
            viewDelegate.minYearDay,
            viewDelegate.maxYear,
            viewDelegate.maxYearMonth,
            viewDelegate.maxYearDay,
            viewDelegate.weekStart
        )
        adapter = WeekViewPagerAdapter()
        addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                //默认的显示星期四，周视图切换就显示星期4
                if (visibility != View.VISIBLE) {
                    isUsingScrollToCalendar = false
                    return
                }
                if (isUsingScrollToCalendar) {
                    isUsingScrollToCalendar = false
                    return
                }
                val view: BaseWeekView? = findViewWithTag(position)
                if (view != null) {
                    view.performClickCalendar(
                        (if (viewDelegate.selectMode != CalendarViewDelegate.SELECT_MODE_DEFAULT)
                            viewDelegate.indexCalendar
                        else
                            viewDelegate.selectedCalendar), !isUsingScrollToCalendar
                    )
                    viewDelegate.weekChangeListener?.onWeekChange(currentWeekCalendars)
                }
                isUsingScrollToCalendar = false
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    /**
     * 获取当前周数据
     *
     * @return 获取当前周数据
     */
    val currentWeekCalendars: List<Calendar>
        get() {
            val calendars = CalendarUtil.getWeekCalendars(viewDelegate.indexCalendar, viewDelegate)
            viewDelegate.addSchemesFromMap(calendars)
            return calendars
        }

    /**
     * 更新周视图
     */
    fun notifyDataSetChanged() {
        weekCount = CalendarUtil.getWeekCountBetweenBothCalendar(
            viewDelegate.minYear,
            viewDelegate.minYearMonth,
            viewDelegate.minYearDay,
            viewDelegate.maxYear,
            viewDelegate.maxYearMonth,
            viewDelegate.maxYearDay,
            viewDelegate.weekStart
        )
        notifyAdapterDataSetChanged()
    }

    /**
     * 更新周视图布局
     */
    fun updateWeekViewClass() {
        isUpdateWeekView = true
        notifyAdapterDataSetChanged()
        isUpdateWeekView = false
    }

    /**
     * 更新日期范围
     */
    fun updateRange() {
        isUpdateWeekView = true
        notifyDataSetChanged()
        isUpdateWeekView = false
        if (visibility != View.VISIBLE) {
            return
        }
        isUsingScrollToCalendar = true
        val calendar = viewDelegate.selectedCalendar
        updateSelected(calendar, false)
        viewDelegate.innerDateSelectedListener?.onWeekDateSelected(calendar, false)
        viewDelegate.calendarSelectListener?.onCalendarSelect(calendar, false)
        val i = CalendarUtil.getWeekFromDayInMonth(calendar, viewDelegate.weekStart)
        parentLayout.updateSelectWeek(i)
    }

    /**
     * 滚动到指定日期
     *
     * @param year  年
     * @param month 月
     * @param day   日
     * @param invokeListener 调用日期事件
     */
    fun scrollToCalendar(
        year: Int,
        month: Int,
        day: Int,
        smoothScroll: Boolean,
        invokeListener: Boolean
    ) {
        isUsingScrollToCalendar = true
        val calendar = Calendar()
        calendar.year = year
        calendar.month = month
        calendar.day = day
        calendar.isCurrentDay = calendar == viewDelegate.currentDay
        LunarCalendar.setupLunarCalendar(calendar)

        viewDelegate.indexCalendar = calendar
        viewDelegate.selectedCalendar = calendar
        viewDelegate.updateSelectCalendarScheme()
        updateSelected(calendar, smoothScroll)

        viewDelegate.innerDateSelectedListener?.onWeekDateSelected(calendar, false)
        if (invokeListener) {
            viewDelegate.calendarSelectListener?.onCalendarSelect(calendar, false)
        }
        val i = CalendarUtil.getWeekFromDayInMonth(calendar, viewDelegate.weekStart)
        parentLayout.updateSelectWeek(i)
    }

    /**
     * 滚动到当前
     */
    fun scrollToCurrent(smoothScroll: Boolean) {
        isUsingScrollToCalendar = true
        val position = CalendarUtil.getWeekFromCalendarStartWithMinCalendar(
            viewDelegate.currentDay,
            viewDelegate.minYear,
            viewDelegate.minYearMonth,
            viewDelegate.minYearDay,
            viewDelegate.weekStart
        ) - 1
        val curItem = currentItem
        if (curItem == position) {
            isUsingScrollToCalendar = false
        }
        setCurrentItem(position, smoothScroll)
        val view: BaseWeekView? = findViewWithTag(position)
        if (view != null) {
            view.performClickCalendar(viewDelegate.currentDay, false)
            view.setSelectedCalendar(viewDelegate.currentDay)
            view.invalidate()
        }
        if (visibility == View.VISIBLE) {
            viewDelegate.calendarSelectListener?.onCalendarSelect(
                viewDelegate.selectedCalendar,
                false
            )
        }
        if (visibility == View.VISIBLE) {
            viewDelegate.innerDateSelectedListener?.onWeekDateSelected(
                viewDelegate.currentDay,
                false
            )
        }
        val i = CalendarUtil.getWeekFromDayInMonth(viewDelegate.currentDay, viewDelegate.weekStart)
        parentLayout.updateSelectWeek(i)
    }

    /**
     * 更新任意一个选择的日期
     */
    fun updateSelected(calendar: Calendar, smoothScroll: Boolean) {
        val position = CalendarUtil.getWeekFromCalendarStartWithMinCalendar(
            calendar,
            viewDelegate.minYear,
            viewDelegate.minYearMonth,
            viewDelegate.minYearDay,
            viewDelegate.weekStart
        ) - 1
        val curItem = currentItem
        isUsingScrollToCalendar = curItem != position
        setCurrentItem(position, smoothScroll)
        val view: BaseWeekView? = findViewWithTag(position)
        if (view != null) {
            view.setSelectedCalendar(calendar)
            view.invalidate()
        }
    }

    /**
     * 更新单选模式
     */
    fun updateSingleSelect() {
        if (viewDelegate.selectMode == CalendarViewDelegate.SELECT_MODE_DEFAULT) {
            return
        }
        for (i in 0 until childCount) {
            val view = getChildAt(i) as BaseWeekView
            view.updateSingleSelect()
        }
    }

    /**
     * 更新为默认选择模式
     */
    fun updateDefaultSelect() {
        val view: BaseWeekView? = findViewWithTag(currentItem)
        if (view != null) {
            view.setSelectedCalendar(viewDelegate.selectedCalendar)
            view.invalidate()
        }
    }

    /**
     * 更新选择效果
     */
    fun updateSelected() {
        for (i in 0 until childCount) {
            val view = getChildAt(i) as BaseWeekView
            view.setSelectedCalendar(viewDelegate.selectedCalendar)
            view.invalidate()
        }
    }

    /**
     * 更新字体颜色大小
     */
    fun updateStyle() {
        for (i in 0 until childCount) {
            val view = getChildAt(i) as BaseWeekView
            view.updateStyle()
            view.invalidate()
        }
    }

    /**
     * 更新标记日期
     */
    fun updateScheme() {
        for (i in 0 until childCount) {
            val view = getChildAt(i) as BaseWeekView
            view.update()
        }
    }

    /**
     * 更新当前日期，夜间过度的时候调用这个函数，一般不需要调用
     */
    fun updateCurrentDate() {
        for (i in 0 until childCount) {
            val view = getChildAt(i) as BaseWeekView
            view.updateCurrentDate()
        }
    }

    /**
     * 更新显示模式
     */
    fun updateShowMode() {
        for (i in 0 until childCount) {
            val view = getChildAt(i) as BaseWeekView
            view.updateShowMode()
        }
    }

    /**
     * 更新周起始
     */
    fun updateWeekStart() {
        if (adapter == null) {
            return
        }
        val count = adapter?.count
        weekCount = CalendarUtil.getWeekCountBetweenBothCalendar(
            viewDelegate.minYear,
            viewDelegate.minYearMonth,
            viewDelegate.minYearDay,
            viewDelegate.maxYear,
            viewDelegate.maxYearMonth,
            viewDelegate.maxYearDay,
            viewDelegate.weekStart
        )
        /*
         * 如果count发生变化，意味着数据源变化，则必须先调用notifyDataSetChanged()，
         * 否则会抛出异常
         */if (count != weekCount) {
            isUpdateWeekView = true
            adapter?.notifyDataSetChanged()
        }
        for (i in 0 until childCount) {
            val view = getChildAt(i) as BaseWeekView
            view.updateWeekStart()
        }
        isUpdateWeekView = false
        updateSelected(viewDelegate.selectedCalendar, false)
    }

    /**
     * 更新高度
     */
    fun updateItemHeight() {
        for (i in 0 until childCount) {
            val view = getChildAt(i) as BaseWeekView
            view.updateItemHeight()
            view.requestLayout()
        }
    }

    /**
     * 清除选择范围
     */
    fun clearSelectRange() {
        for (i in 0 until childCount) {
            val view = getChildAt(i) as BaseWeekView
            view.invalidate()
        }
    }

    fun clearSingleSelect() {
        for (i in 0 until childCount) {
            val view = getChildAt(i) as BaseWeekView
            view.currentItem = -1
            view.invalidate()
        }
    }

    fun clearMultiSelect() {
        for (i in 0 until childCount) {
            val view = getChildAt(i) as BaseWeekView
            view.currentItem = -1
            view.invalidate()
        }
    }

    private fun notifyAdapterDataSetChanged() {
        adapter?.notifyDataSetChanged()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return viewDelegate.isWeekViewScrollable && super.onTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return viewDelegate.isWeekViewScrollable && super.onInterceptTouchEvent(ev)
    }

    /**
     * 周视图的高度应该与日历项的高度一致
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val newHeightMeasureSpec =
            MeasureSpec.makeMeasureSpec(viewDelegate.calendarItemHeight, MeasureSpec.EXACTLY)
        super.onMeasure(widthMeasureSpec, newHeightMeasureSpec)
    }

    /**
     * 周视图切换
     */
    private inner class WeekViewPagerAdapter : PagerAdapter() {
        override fun getCount(): Int {
            return weekCount
        }

        override fun getItemPosition(`object`: Any): Int {
            return if (isUpdateWeekView) POSITION_NONE else super.getItemPosition(`object`)
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view == `object`
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val calendar = CalendarUtil.getFirstCalendarStartWithMinCalendar(
                viewDelegate.minYear,
                viewDelegate.minYearMonth,
                viewDelegate.minYearDay,
                position + 1,
                viewDelegate.weekStart
            )

            val view = try {
                val constructor = viewDelegate.weekViewClass?.getConstructor(Context::class.java)
                constructor?.newInstance(context) as? BaseWeekView
                    ?: throw IllegalStateException("must instanceof BaseWeekView")
            } catch (e: Exception) {
                e.printStackTrace()
                return DefaultWeekView(context)
            }

            view.parentLayout = parentLayout
            view.setup(viewDelegate)
            view.setup(calendar)
            view.tag = position
            view.setSelectedCalendar(viewDelegate.selectedCalendar)
            container.addView(view)
            return view
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            (`object` as? BaseWeekView)?.apply {
                onDestroy()
                container.removeView(this)
            }
        }
    }
}