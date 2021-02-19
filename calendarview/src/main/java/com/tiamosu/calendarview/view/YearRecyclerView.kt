package com.tiamosu.calendarview.view

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tiamosu.calendarview.adapter.BaseRecyclerAdapter
import com.tiamosu.calendarview.adapter.YearViewAdapter
import com.tiamosu.calendarview.delegate.CalendarViewDelegate
import com.tiamosu.calendarview.entity.Month
import com.tiamosu.calendarview.utils.CalendarUtil
import java.util.*

/**
 * 年份布局选择View
 *
 * @author tiamosu
 * @date 2020/5/25.
 */
class YearRecyclerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : RecyclerView(context, attrs) {
    private lateinit var viewDelegate: CalendarViewDelegate
    private val viewAdapter = YearViewAdapter(context)
    private var monthSelectedListener: OnMonthSelectedListener? = null

    init {
        layoutManager = GridLayoutManager(context, 3)
        adapter = viewAdapter
        viewAdapter.setOnItemClickListener(object : BaseRecyclerAdapter.OnItemClickListener {
            override fun onItemClick(position: Int, itemId: Long) {
                if (monthSelectedListener != null) {
                    val month = viewAdapter.getItem(position) ?: return
                    if (!CalendarUtil.isMonthInRange(
                            month.year, month.month,
                            viewDelegate.minYear, viewDelegate.minYearMonth,
                            viewDelegate.maxYear, viewDelegate.maxYearMonth
                        )
                    ) {
                        return
                    }
                    monthSelectedListener?.onMonthSelected(month.year, month.month)
                    viewDelegate.yearViewChangeListener?.onYearViewChange(true)
                }
            }
        })
    }

    /**
     * 设置
     *
     * @param delegate delegate
     */
    fun setup(delegate: CalendarViewDelegate) {
        viewDelegate = delegate
        this.viewAdapter.setup(delegate)
    }

    /**
     * 初始化年视图
     */
    fun init(year: Int) {
        val date = Calendar.getInstance()
        for (i in 1..12) {
            date[year, i - 1] = 1
            val mDaysCount = CalendarUtil.getMonthDaysCount(year, i)
            val month = Month()
            month.diff = CalendarUtil.getMonthViewStartDiff(year, i, viewDelegate.weekStart)
            month.count = mDaysCount
            month.month = i
            month.year = year
            viewAdapter.addItem(month)
        }
    }

    /**
     * 更新周起始
     */
    fun updateWeekStart() {
        for (month in viewAdapter.getItems()) {
            month.diff =
                CalendarUtil.getMonthViewStartDiff(month.year, month.month, viewDelegate.weekStart)
        }
    }

    /**
     * 更新字体颜色大小
     */
    fun updateStyle() {
        for (i in 0 until childCount) {
            val view = getChildAt(i) as YearView
            view.updateStyle()
            view.invalidate()
        }
    }

    /**
     * 月份选择事件
     *
     * @param listener listener
     */
    fun setOnMonthSelectedListener(listener: OnMonthSelectedListener?) {
        monthSelectedListener = listener
    }

    fun notifyAdapterDataSetChanged() {
        adapter?.notifyDataSetChanged()
    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        super.onMeasure(widthSpec, heightSpec)
        val height = MeasureSpec.getSize(heightSpec)
        val width = MeasureSpec.getSize(widthSpec)
        viewAdapter.setYearViewSize(width / 3, height / 4)
    }

    fun interface OnMonthSelectedListener {
        fun onMonthSelected(year: Int, month: Int)
    }
}