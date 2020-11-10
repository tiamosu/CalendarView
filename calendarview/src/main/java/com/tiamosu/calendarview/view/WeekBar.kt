package com.tiamosu.calendarview.view

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import com.tiamosu.calendarview.R
import com.tiamosu.calendarview.delegate.CalendarViewDelegate
import com.tiamosu.calendarview.entity.Calendar

/**
 * 星期栏，如果你要使用星期栏自定义，切记XML使用 merge，不要使用 LinearLayout
 *
 * @author tiamosu
 * @date 2020/5/25.
 */
open class WeekBar(context: Context) : LinearLayoutCompat(context) {
    private lateinit var viewDelegate: CalendarViewDelegate

    open fun getLayoutId() = R.layout.layout_calendar_week_bar

    init {
        initLayout()
    }

    private fun initLayout() {
        setContentView()
    }

    open fun setContentView() {
        LayoutInflater.from(context).inflate(getLayoutId(), this, true)
    }

    /**
     * 传递属性
     *
     * @param delegate delegate
     */
    open fun setup(delegate: CalendarViewDelegate) {
        viewDelegate = delegate
        setTextSize(viewDelegate.weekTextSize)
        setTextColor(delegate.weekTextColor)
        setBackgroundColor(delegate.weekBackground)
        setPadding(delegate.calendarPadding, 0, delegate.calendarPadding, 0)
    }

    /**
     * 设置文本颜色，使用自定义布局需要重写这个方法，避免出问题
     * 如果这里报错了，请确定你自定义XML文件跟布局是不是使用merge，而不是LinearLayout
     *
     * @param color color
     */
    open fun setTextColor(color: Int) {
        for (i in 0 until childCount) {
            (getChildAt(i) as? TextView)?.setTextColor(color)
        }
    }

    /**
     * 设置文本大小
     *
     * @param size size
     */
    open fun setTextSize(size: Int) {
        for (i in 0 until childCount) {
            (getChildAt(i) as? TextView)?.setTextSize(TypedValue.COMPLEX_UNIT_PX, size.toFloat())
        }
    }

    /**
     * 日期选择事件，这里提供这个回调，可以方便定制WeekBar需要
     *
     * @param calendar  calendar 选择的日期
     * @param weekStart 周起始
     * @param isClick   isClick 点击
     */
    open fun onDateSelected(calendar: Calendar, weekStart: Int, isClick: Boolean) {}

    /**
     * 当周起始发生变化，使用自定义布局需要重写这个方法，避免出问题
     *
     * @param weekStart 周起始
     */
    open fun onWeekStartChange(weekStart: Int) {
        for (i in 0 until childCount) {
            (getChildAt(i) as TextView).text = getWeekString(i, weekStart)
        }
    }

    /**
     * 通过View的位置和周起始获取星期的对应坐标
     *
     * @param calendar  calendar
     * @param weekStart weekStart
     * @return 通过View的位置和周起始获取星期的对应坐标
     */
    protected fun getViewIndexByCalendar(calendar: Calendar, weekStart: Int): Int {
        val week = calendar.week + 1
        if (weekStart == CalendarViewDelegate.WEEK_START_WITH_SUN) {
            return week - 1
        }
        if (weekStart == CalendarViewDelegate.WEEK_START_WITH_MON) {
            return if (week == CalendarViewDelegate.WEEK_START_WITH_SUN) 6 else week - 2
        }
        return if (week == CalendarViewDelegate.WEEK_START_WITH_SAT) 0 else week
    }

    /**
     * 或者周文本，这个方法仅供父类使用
     *
     * @param index     index
     * @param weekStart weekStart
     * @return 或者周文本
     */
    private fun getWeekString(index: Int, weekStart: Int): String {
        val weeks = context.resources.getStringArray(R.array.week_string_array)
        if (weekStart == CalendarViewDelegate.WEEK_START_WITH_SUN) {
            return weeks[index]
        }
        return if (weekStart == CalendarViewDelegate.WEEK_START_WITH_MON) {
            weeks[if (index == 6) 0 else index + 1]
        } else weeks[if (index == 0) 6 else index - 1]
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val newHeightMeasureSpec: Int =
            MeasureSpec.makeMeasureSpec(viewDelegate.weekBarHeight, MeasureSpec.EXACTLY)
        super.onMeasure(widthMeasureSpec, newHeightMeasureSpec)
    }
}