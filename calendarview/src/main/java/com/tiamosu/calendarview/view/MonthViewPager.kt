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
import kotlin.math.abs

/**
 * 月份切换ViewPager，自定义适应高度
 *
 * @author tiamosu
 * @date 2020/5/25.
 */
class MonthViewPager @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ViewPager(context, attrs) {

    private var isUpdateMonthView = false
    private var monthCount = 0
    private lateinit var viewDelegate: CalendarViewDelegate
    private var nextViewHeight = 0
    private var preViewHeight = 0
    private var currentViewHeight = 0
    var parentLayout: CalendarLayout? = null
    lateinit var weekPager: WeekViewPager
    lateinit var weekBar: WeekBar

    /**
     * 是否使用滚动到某一天
     */
    private var isUsingScrollToCalendar = false

    /**
     * 初始化
     *
     * @param delegate delegate
     */
    fun setup(delegate: CalendarViewDelegate) {
        viewDelegate = delegate
        updateMonthViewHeight(
            viewDelegate.currentDay.year,
            viewDelegate.currentDay.month
        )
        val params = layoutParams
        params.height = currentViewHeight
        layoutParams = params
        init()
    }

    /**
     * 初始化
     */
    private fun init() {
        monthCount = (12 * (viewDelegate.maxYear - viewDelegate.minYear)
                - viewDelegate.minYearMonth) + 1 + viewDelegate.maxYearMonth
        adapter = MonthViewPagerAdapter()
        addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                if (viewDelegate.monthViewShowMode == CalendarViewDelegate.MODE_ALL_MONTH) {
                    return
                }
                val height = if (position < currentItem) { //右滑-1
                    (preViewHeight
                            * (1 - positionOffset) +
                            currentViewHeight
                            * positionOffset).toInt()
                } else { //左滑+！
                    (currentViewHeight
                            * (1 - positionOffset) +
                            nextViewHeight
                            * positionOffset).toInt()
                }
                val params = layoutParams
                params.height = height
                layoutParams = params
            }

            override fun onPageSelected(position: Int) {
                val calendar =
                    CalendarUtil.getFirstCalendarFromMonthViewPager(position, viewDelegate)
                if (visibility == View.VISIBLE) {
                    if (!viewDelegate.isShowYearSelectedLayout
                        && calendar.year != viewDelegate.indexCalendar.year
                    ) {
                        viewDelegate.yearChangeListener?.onYearChange(calendar.year)
                    }
                    viewDelegate.indexCalendar = calendar
                }
                //月份改变事件
                viewDelegate.monthChangeListener?.onMonthChange(calendar.year, calendar.month)

                //周视图显示的时候就需要动态改变月视图高度
                if (weekPager.visibility == View.VISIBLE) {
                    updateMonthViewHeight(calendar.year, calendar.month)
                    return
                }
                if (viewDelegate.selectMode == CalendarViewDelegate.SELECT_MODE_DEFAULT) {
                    if (!calendar.isCurrentMonth) {
                        viewDelegate.selectedCalendar = calendar
                    } else {
                        viewDelegate.selectedCalendar =
                            CalendarUtil.getRangeEdgeCalendar(calendar, viewDelegate)
                    }
                    viewDelegate.indexCalendar = viewDelegate.selectedCalendar
                } else {
                    if (viewDelegate.selectedStartRangeCalendar?.isSameMonth(viewDelegate.indexCalendar) == true) {
                        viewDelegate.indexCalendar = viewDelegate.selectedStartRangeCalendar!!
                    } else {
                        if (calendar.isSameMonth(viewDelegate.selectedCalendar)) {
                            viewDelegate.indexCalendar = viewDelegate.selectedCalendar
                        }
                    }
                }
                viewDelegate.updateSelectCalendarScheme()
                if (!isUsingScrollToCalendar && viewDelegate.selectMode == CalendarViewDelegate.SELECT_MODE_DEFAULT) {
                    weekBar.onDateSelected(
                        viewDelegate.selectedCalendar,
                        viewDelegate.weekStart,
                        false
                    )
                    viewDelegate.calendarSelectListener?.onCalendarSelect(
                        viewDelegate.selectedCalendar,
                        false
                    )
                }
                val view: BaseMonthView? = findViewWithTag(position)
                if (view != null) {
                    val index = view.getSelectedIndex(viewDelegate.indexCalendar)
                    if (viewDelegate.selectMode == CalendarViewDelegate.SELECT_MODE_DEFAULT) {
                        view.currentItem = index
                    }
                    if (index >= 0) {
                        parentLayout?.updateSelectPosition(index)
                    }
                    view.invalidate()
                }
                weekPager.updateSelected(viewDelegate.indexCalendar, false)
                updateMonthViewHeight(calendar.year, calendar.month)
                isUsingScrollToCalendar = false
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    /**
     * 更新月视图的高度
     */
    private fun updateMonthViewHeight(year: Int, month: Int) {
        if (viewDelegate.monthViewShowMode == CalendarViewDelegate.MODE_ALL_MONTH) { //非动态高度就不需要了
            currentViewHeight = 6 * viewDelegate.calendarItemHeight
            val params = layoutParams
            params.height = currentViewHeight
            return
        }
        if (visibility != View.VISIBLE) { //如果已经显示周视图，则需要动态改变月视图高度，否则显示就有bug
            val params = layoutParams
            params.height = CalendarUtil.getMonthViewHeight(
                year, month,
                viewDelegate.calendarItemHeight, viewDelegate.weekStart,
                viewDelegate.monthViewShowMode
            )
            layoutParams = params
        }
        parentLayout?.updateContentViewTranslateY()

        currentViewHeight = CalendarUtil.getMonthViewHeight(
            year, month,
            viewDelegate.calendarItemHeight, viewDelegate.weekStart,
            viewDelegate.monthViewShowMode
        )
        if (month == 1) {
            preViewHeight = CalendarUtil.getMonthViewHeight(
                year - 1, 12,
                viewDelegate.calendarItemHeight, viewDelegate.weekStart,
                viewDelegate.monthViewShowMode
            )
            nextViewHeight = CalendarUtil.getMonthViewHeight(
                year, 2,
                viewDelegate.calendarItemHeight, viewDelegate.weekStart,
                viewDelegate.monthViewShowMode
            )
        } else {
            preViewHeight = CalendarUtil.getMonthViewHeight(
                year, month - 1,
                viewDelegate.calendarItemHeight, viewDelegate.weekStart,
                viewDelegate.monthViewShowMode
            )
            nextViewHeight = if (month == 12) {
                CalendarUtil.getMonthViewHeight(
                    year + 1, 1,
                    viewDelegate.calendarItemHeight, viewDelegate.weekStart,
                    viewDelegate.monthViewShowMode
                )
            } else {
                CalendarUtil.getMonthViewHeight(
                    year, month + 1,
                    viewDelegate.calendarItemHeight, viewDelegate.weekStart,
                    viewDelegate.monthViewShowMode
                )
            }
        }
    }

    /**
     * 刷新
     */
    fun notifyDataSetChanged() {
        monthCount = (12 * (viewDelegate.maxYear - viewDelegate.minYear)
                - viewDelegate.minYearMonth) + 1 +
                viewDelegate.maxYearMonth
        notifyAdapterDataSetChanged()
    }

    /**
     * 更新月视图Class
     */
    fun updateMonthViewClass() {
        isUpdateMonthView = true
        notifyAdapterDataSetChanged()
        isUpdateMonthView = false
    }

    /**
     * 更新日期范围
     */
    fun updateRange() {
        isUpdateMonthView = true
        notifyDataSetChanged()
        isUpdateMonthView = false
        if (visibility != View.VISIBLE) {
            return
        }
        isUsingScrollToCalendar = false
        val calendar = viewDelegate.selectedCalendar
        val y = calendar.year - viewDelegate.minYear
        val position = 12 * y + calendar.month - viewDelegate.minYearMonth
        setCurrentItem(position, false)
        val view: BaseMonthView? = findViewWithTag(position)
        if (view != null) {
            view.setSelectedCalendar(viewDelegate.indexCalendar)
            view.invalidate()
            parentLayout?.updateSelectPosition(view.getSelectedIndex(viewDelegate.indexCalendar))
        }
        val week = CalendarUtil.getWeekFromDayInMonth(calendar, viewDelegate.weekStart)
        parentLayout?.updateSelectWeek(week)
        viewDelegate.innerDateSelectedListener?.onMonthDateSelected(calendar, false)
        viewDelegate.calendarSelectListener?.onCalendarSelect(calendar, false)
        updateSelected()
    }

    /**
     * 滚动到指定日期
     *
     * @param year           年
     * @param month          月
     * @param day            日
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

        val y = calendar.year - viewDelegate.minYear
        val position = 12 * y + calendar.month - viewDelegate.minYearMonth
        val curItem = currentItem
        if (curItem == position) {
            isUsingScrollToCalendar = false
        }
        setCurrentItem(position, smoothScroll)

        val view: BaseMonthView? = findViewWithTag(position)
        if (view != null) {
            view.setSelectedCalendar(viewDelegate.indexCalendar)
            view.invalidate()
            parentLayout?.updateSelectPosition(view.getSelectedIndex(viewDelegate.indexCalendar))
        }
        val week = CalendarUtil.getWeekFromDayInMonth(calendar, viewDelegate.weekStart)
        parentLayout?.updateSelectWeek(week)
        if (invokeListener) {
            viewDelegate.calendarSelectListener?.onCalendarSelect(calendar, false)
        }
        viewDelegate.innerDateSelectedListener?.onMonthDateSelected(calendar, false)
        updateSelected()
    }

    /**
     * 滚动到当前日期
     */
    fun scrollToCurrent(smoothScroll: Boolean) {
        isUsingScrollToCalendar = true
        val position = 12 * (viewDelegate.currentDay.year - viewDelegate.minYear) +
                viewDelegate.currentDay.month - viewDelegate.minYearMonth
        val curItem = currentItem
        if (curItem == position) {
            isUsingScrollToCalendar = false
        }
        setCurrentItem(position, smoothScroll)

        val view: BaseMonthView? = findViewWithTag(position)
        if (view != null) {
            view.setSelectedCalendar(viewDelegate.currentDay)
            view.invalidate()
            parentLayout?.updateSelectPosition(view.getSelectedIndex(viewDelegate.currentDay))
        }
        if (visibility == View.VISIBLE) {
            viewDelegate.calendarSelectListener?.onCalendarSelect(
                viewDelegate.selectedCalendar,
                false
            )
        }
    }

    /**
     * 获取当前月份数据
     *
     * @return 获取当前月份数据
     */
    val currentMonthCalendars: List<Calendar>?
        get() {
            val view: BaseMonthView? = findViewWithTag(currentItem) ?: return null
            return view?.items
        }

    /**
     * 更新为默认选择模式
     */
    fun updateDefaultSelect() {
        val view: BaseMonthView? = findViewWithTag(currentItem)
        if (view != null) {
            val index = view.getSelectedIndex(viewDelegate.selectedCalendar)
            view.currentItem = index
            if (index >= 0) {
                parentLayout?.updateSelectPosition(index)
            }
            view.invalidate()
        }
    }

    /**
     * 更新选择效果
     */
    fun updateSelected() {
        for (i in 0 until childCount) {
            val view = getChildAt(i) as BaseMonthView
            view.setSelectedCalendar(viewDelegate.selectedCalendar)
            view.invalidate()
        }
    }

    /**
     * 更新字体颜色大小
     */
    fun updateStyle() {
        for (i in 0 until childCount) {
            val view = getChildAt(i) as BaseMonthView
            view.updateStyle()
            view.invalidate()
        }
    }

    /**
     * 更新标记日期
     */
    fun updateScheme() {
        for (i in 0 until childCount) {
            val view = getChildAt(i) as BaseMonthView
            view.update()
        }
    }

    /**
     * 更新当前日期，夜间过度的时候调用这个函数，一般不需要调用
     */
    fun updateCurrentDate() {
        for (i in 0 until childCount) {
            val view = getChildAt(i) as BaseMonthView
            view.updateCurrentDate()
        }
    }

    /**
     * 更新显示模式
     */
    fun updateShowMode() {
        for (i in 0 until childCount) {
            val view = getChildAt(i) as BaseMonthView
            view.updateShowMode()
            view.requestLayout()
        }
        if (viewDelegate.monthViewShowMode == CalendarViewDelegate.MODE_ALL_MONTH) {
            currentViewHeight = 6 * viewDelegate.calendarItemHeight
            nextViewHeight = currentViewHeight
            preViewHeight = currentViewHeight
        } else {
            updateMonthViewHeight(
                viewDelegate.selectedCalendar.year,
                viewDelegate.selectedCalendar.month
            )
        }
        val params = layoutParams
        params.height = currentViewHeight
        layoutParams = params
        parentLayout?.updateContentViewTranslateY()
    }

    /**
     * 更新周起始
     */
    fun updateWeekStart() {
        for (i in 0 until childCount) {
            val view = getChildAt(i) as BaseMonthView
            view.updateWeekStart()
            view.requestLayout()
        }
        updateMonthViewHeight(
            viewDelegate.selectedCalendar.year,
            viewDelegate.selectedCalendar.month
        )
        val params = layoutParams
        params.height = currentViewHeight
        layoutParams = params
        val i = CalendarUtil.getWeekFromDayInMonth(
            viewDelegate.selectedCalendar,
            viewDelegate.weekStart
        )
        parentLayout?.updateSelectWeek(i)
        updateSelected()
    }

    /**
     * 更新高度
     */
    fun updateItemHeight() {
        for (i in 0 until childCount) {
            val view = getChildAt(i) as BaseMonthView
            view.updateItemHeight()
            view.requestLayout()
        }
        val year = viewDelegate.indexCalendar.year
        val month = viewDelegate.indexCalendar.month
        currentViewHeight = CalendarUtil.getMonthViewHeight(
            year, month,
            viewDelegate.calendarItemHeight, viewDelegate.weekStart,
            viewDelegate.monthViewShowMode
        )
        if (month == 1) {
            preViewHeight = CalendarUtil.getMonthViewHeight(
                year - 1, 12,
                viewDelegate.calendarItemHeight, viewDelegate.weekStart,
                viewDelegate.monthViewShowMode
            )
            nextViewHeight = CalendarUtil.getMonthViewHeight(
                year, 2,
                viewDelegate.calendarItemHeight, viewDelegate.weekStart,
                viewDelegate.monthViewShowMode
            )
        } else {
            preViewHeight = CalendarUtil.getMonthViewHeight(
                year, month - 1,
                viewDelegate.calendarItemHeight, viewDelegate.weekStart,
                viewDelegate.monthViewShowMode
            )
            nextViewHeight = if (month == 12) {
                CalendarUtil.getMonthViewHeight(
                    year + 1, 1,
                    viewDelegate.calendarItemHeight, viewDelegate.weekStart,
                    viewDelegate.monthViewShowMode
                )
            } else {
                CalendarUtil.getMonthViewHeight(
                    year, month + 1,
                    viewDelegate.calendarItemHeight, viewDelegate.weekStart,
                    viewDelegate.monthViewShowMode
                )
            }
        }
        val params = layoutParams
        params.height = currentViewHeight
        layoutParams = params
    }

    /**
     * 清除选择范围
     */
    fun clearSelectRange() {
        for (i in 0 until childCount) {
            val view = getChildAt(i) as BaseMonthView
            view.invalidate()
        }
    }

    /**
     * 清除单选选择
     */
    fun clearSingleSelect() {
        for (i in 0 until childCount) {
            val view = getChildAt(i) as BaseMonthView
            view.currentItem = -1
            view.invalidate()
        }
    }

    /**
     * 清除单选选择
     */
    fun clearMultiSelect() {
        for (i in 0 until childCount) {
            val view = getChildAt(i) as BaseMonthView
            view.currentItem = -1
            view.invalidate()
        }
    }

    private fun notifyAdapterDataSetChanged() {
        adapter?.notifyDataSetChanged()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return viewDelegate.isMonthViewScrollable && super.onTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return viewDelegate.isMonthViewScrollable && super.onInterceptTouchEvent(ev)
    }

    override fun setCurrentItem(item: Int) {
        setCurrentItem(item, true)
    }

    override fun setCurrentItem(item: Int, smoothScroll: Boolean) {
        if (abs(currentItem - item) > 1) {
            super.setCurrentItem(item, false)
        } else {
            super.setCurrentItem(item, smoothScroll)
        }
    }

    /**
     * 日历卡月份Adapter
     */
    private inner class MonthViewPagerAdapter : PagerAdapter() {
        override fun getCount(): Int {
            return monthCount
        }

        override fun getItemPosition(`object`: Any): Int {
            return if (isUpdateMonthView) POSITION_NONE else super.getItemPosition(`object`)
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view == `object`
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val year = (position + viewDelegate.minYearMonth - 1) / 12 + viewDelegate.minYear
            val month = (position + viewDelegate.minYearMonth - 1) % 12 + 1
            val view = try {
                val constructor = viewDelegate.monthViewClass?.getConstructor(Context::class.java)
                constructor?.newInstance(context) as? BaseMonthView
                    ?: throw IllegalStateException("must instanceof BaseMonthView")
            } catch (e: Exception) {
                e.printStackTrace()
                return DefaultMonthView(context)
            }
            view.monthViewPager = this@MonthViewPager
            view.parentLayout = parentLayout
            view.setup(viewDelegate)
            view.tag = position
            view.initMonthWithDate(year, month)
            view.setSelectedCalendar(viewDelegate.selectedCalendar)
            container.addView(view)
            return view
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            val view = `object` as BaseView
            view.onDestroy()
            container.removeView(view)
        }
    }
}