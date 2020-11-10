package com.tiamosu.calendarview.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tiamosu.calendarview.delegate.CalendarViewDelegate
import com.tiamosu.calendarview.entity.Month
import com.tiamosu.calendarview.view.DefaultYearView
import com.tiamosu.calendarview.view.YearView

/**
 * @author tiamosu
 * @date 2020/5/25.
 */
internal class YearViewAdapter(private val context: Context) : BaseRecyclerAdapter<Month>() {
    private lateinit var viewDelegate: CalendarViewDelegate
    private var itemWidth = 0
    private var itemHeight = 0

    fun setup(delegate: CalendarViewDelegate) {
        viewDelegate = delegate
    }

    fun setYearViewSize(width: Int, height: Int) {
        itemWidth = width
        itemHeight = height
    }

    override fun onCreateDefaultViewHolder(parent: ViewGroup?, type: Int): RecyclerView.ViewHolder {
        val yearView = if (viewDelegate.yearViewClassPath.isNullOrBlank()) {
            DefaultYearView(context)
        } else {
            try {
                val constructor = viewDelegate.yearViewClass?.getConstructor(Context::class.java)
                constructor?.newInstance(context) as? YearView
                    ?: throw IllegalStateException("must instanceof YearView")
            } catch (e: Exception) {
                e.printStackTrace()
                DefaultYearView(context)
            }
        }
        val params = RecyclerView.LayoutParams(
            RecyclerView.LayoutParams.MATCH_PARENT,
            RecyclerView.LayoutParams.MATCH_PARENT
        )
        yearView.layoutParams = params
        return YearViewHolder(yearView, viewDelegate)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, item: Month, position: Int) {
        val yearViewHolder = holder as? YearViewHolder
        val view = yearViewHolder?.yearView
        view?.init(item.year, item.month)
        view?.measureSize(itemWidth, itemHeight)
    }

    private class YearViewHolder(itemView: View, delegate: CalendarViewDelegate) :
        RecyclerView.ViewHolder(itemView) {
        var yearView = itemView as? YearView

        init {
            yearView?.setup(delegate)
        }
    }
}