package com.tiamosu.calendarview.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.tiamosu.calendarview.delegate.CalendarViewDelegate
import kotlin.math.abs

/**
 * 年份 + 月份选择布局
 * ViewPager + RecyclerView
 *
 * @author tiamosu
 * @date 2020/5/25.
 */
class YearViewPager @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ViewPager(context, attrs) {

    private var yearCount = 0
    private var isUpdateYearView = false
    private lateinit var viewDelegate: CalendarViewDelegate
    private var monthSelectedListener: YearRecyclerView.OnMonthSelectedListener? = null

    fun setup(delegate: CalendarViewDelegate) {
        viewDelegate = delegate
        yearCount = viewDelegate.maxYear - viewDelegate.minYear + 1
        adapter = object : PagerAdapter() {
            override fun getCount(): Int {
                return yearCount
            }

            override fun getItemPosition(`object`: Any): Int {
                return if (isUpdateYearView) POSITION_NONE else super.getItemPosition(`object`)
            }

            override fun isViewFromObject(view: View, `object`: Any): Boolean {
                return view === `object`
            }

            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                val view = YearRecyclerView(context)
                container.addView(view)
                view.setup(viewDelegate)
                view.setOnMonthSelectedListener(monthSelectedListener)
                view.init(position + viewDelegate.minYear)
                return view
            }

            override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
                container.removeView(`object` as View)
            }
        }
        currentItem = viewDelegate.currentDay.year - viewDelegate.minYear
    }

    override fun setCurrentItem(item: Int) {
        setCurrentItem(item, false)
    }

    override fun setCurrentItem(item: Int, smoothScroll: Boolean) {
        if (abs(currentItem - item) > 1) {
            super.setCurrentItem(item, false)
        } else {
            super.setCurrentItem(item, false)
        }
    }

    /**
     * 通知刷新
     */
    fun notifyDataSetChanged() {
        yearCount = viewDelegate.maxYear - viewDelegate.minYear + 1
        adapter?.notifyDataSetChanged()
    }

    /**
     * 滚动到某年
     */
    fun scrollToYear(year: Int, smoothScroll: Boolean) {
        setCurrentItem(year - viewDelegate.minYear, smoothScroll)
    }

    /**
     * 更新日期范围
     */
    fun updateRange() {
        isUpdateYearView = true
        notifyDataSetChanged()
        isUpdateYearView = false
    }

    /**
     * 更新界面
     */
    fun update() {
        for (i in 0 until childCount) {
            val view = getChildAt(i) as YearRecyclerView
            view.notifyAdapterDataSetChanged()
        }
    }

    /**
     * 更新周起始
     */
    fun updateWeekStart() {
        for (i in 0 until childCount) {
            val view = getChildAt(i) as YearRecyclerView
            view.updateWeekStart()
            view.notifyAdapterDataSetChanged()
        }
    }

    /**
     * 更新字体颜色大小
     */
    fun updateStyle() {
        for (i in 0 until childCount) {
            val view = getChildAt(i) as YearRecyclerView
            view.updateStyle()
        }
    }

    fun setOnMonthSelectedListener(listener: YearRecyclerView.OnMonthSelectedListener?) {
        monthSelectedListener = listener
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return viewDelegate.isYearViewScrollable && super.onTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return viewDelegate.isYearViewScrollable && super.onInterceptTouchEvent(ev)
    }

    companion object {

        /**
         * 计算相对高度
         *
         * @return 年月视图选择器最适合的高度
         */
        @Suppress("DEPRECATION")
        private fun getHeight(view: View): Int {
            val windowManager =
                ContextCompat.getSystemService(view.context, WindowManager::class.java)
            val display = windowManager?.defaultDisplay
            val point = Point()
            display?.getSize(point)
            val h = point.y
            val location = IntArray(2)
            view.getLocationInWindow(location)
            view.getLocationOnScreen(location)
            return h - location[1]
        }
    }
}