package com.tiamosu.calendarview.view

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.View.OnLongClickListener
import com.tiamosu.calendarview.CalendarLayout
import com.tiamosu.calendarview.delegate.CalendarViewDelegate
import com.tiamosu.calendarview.entity.Calendar
import com.tiamosu.calendarview.utils.CalendarUtil
import kotlin.math.abs

/**
 * 基本的日历 View，派生出 MonthView 和 WeekView
 *
 * @author tiamosu
 * @date 2020/5/25.
 */
abstract class BaseView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null
) : View(context, attrs), View.OnClickListener, OnLongClickListener {

    companion object {
        /**
         * 字体大小
         */
        private const val TEXT_SIZE = 14
    }

    lateinit var viewDelegate: CalendarViewDelegate

    /**
     * 当前月份日期画笔
     */
    protected var curMonthTextPaint = Paint()

    /**
     * 当前月份农历画笔
     */
    protected var curMonthLunarTextPaint = Paint()

    /**
     * 其它月份日期画笔
     */
    protected var otherMonthTextPaint = Paint()

    /**
     * 其它月份农历画笔
     */
    protected var otherMonthLunarTextPaint = Paint()

    /**
     * 带标记的日期画笔
     */
    protected var schemeTextPaint = Paint()

    /**
     * 带标记的农历画笔
     */
    protected var schemeLunarTextPaint = Paint()

    /**
     * 带标记的 item 背景画笔
     */
    protected var schemePaint = Paint()

    /**
     * 被选中的日期画笔
     */
    protected var selectTextPaint = Paint()

    /**
     * 被选中的农历画笔
     */
    protected var selectedLunarTextPaint = Paint()

    /**
     * 被选择的 item 背景画笔
     */
    protected var selectedItemPaint = Paint()

    /**
     * 当前日期画笔（今天日期）
     */
    protected var curDayTextPaint = Paint()

    /**
     * 当前日期农历画笔（今天农历）
     */
    protected var curDayLunarTextPaint = Paint()

    /**
     * 日历布局，需要在日历下方放自己的布局
     */
    var parentLayout: CalendarLayout? = null

    /**
     * 日历项
     */
    lateinit var items: List<Calendar>

    /**
     * 每一项的高度
     */
    protected var itemHeight = 0

    /**
     * 每一项的宽度
     */
    protected var itemWidth = 0

    /**
     * Text的基线
     */
    protected var textBaseLine = 0f

    /**
     * 点击的x、y坐标
     */
    var mX = 0f
    var mY = 0f

    /**
     * 是否点击
     */
    var isClick = true

    /**
     * 当前点击项
     */
    var currentItem = -1

    init {
        initPaint(context)
    }

    /**
     * 初始化配置
     */
    private fun initPaint(context: Context) {
        curMonthTextPaint.isAntiAlias = true
        curMonthTextPaint.style = Paint.Style.FILL
        curMonthTextPaint.textAlign = Paint.Align.CENTER
        curMonthTextPaint.color = -0xeeeeef
        curMonthTextPaint.isFakeBoldText = true
        curMonthTextPaint.textSize = CalendarUtil.dipToPx(context, TEXT_SIZE.toFloat()).toFloat()

        curMonthLunarTextPaint.isAntiAlias = true
        curMonthLunarTextPaint.style = Paint.Style.FILL
        curMonthLunarTextPaint.textAlign = Paint.Align.CENTER

        otherMonthTextPaint.isAntiAlias = true
        otherMonthTextPaint.style = Paint.Style.FILL
        otherMonthTextPaint.textAlign = Paint.Align.CENTER
        otherMonthTextPaint.color = -0x1e1e1f
        otherMonthTextPaint.isFakeBoldText = true
        otherMonthTextPaint.textSize = CalendarUtil.dipToPx(context, TEXT_SIZE.toFloat()).toFloat()

        otherMonthLunarTextPaint.isAntiAlias = true
        otherMonthLunarTextPaint.style = Paint.Style.FILL
        otherMonthLunarTextPaint.textAlign = Paint.Align.CENTER

        schemeTextPaint.isAntiAlias = true
        schemeTextPaint.style = Paint.Style.FILL
        schemeTextPaint.textAlign = Paint.Align.CENTER
        schemeTextPaint.color = -0x12acad
        schemeTextPaint.isFakeBoldText = true
        schemeTextPaint.textSize = CalendarUtil.dipToPx(context, TEXT_SIZE.toFloat()).toFloat()

        schemeLunarTextPaint.isAntiAlias = true
        schemeLunarTextPaint.style = Paint.Style.FILL
        schemeLunarTextPaint.textAlign = Paint.Align.CENTER

        schemePaint.isAntiAlias = true
        schemePaint.style = Paint.Style.FILL
        schemePaint.strokeWidth = 2f
        schemePaint.color = -0x101011

        selectTextPaint.isAntiAlias = true
        selectTextPaint.style = Paint.Style.FILL
        selectTextPaint.textAlign = Paint.Align.CENTER
        selectTextPaint.color = -0x12acad
        selectTextPaint.isFakeBoldText = true
        selectTextPaint.textSize = CalendarUtil.dipToPx(context, TEXT_SIZE.toFloat()).toFloat()

        selectedLunarTextPaint.isAntiAlias = true
        selectedLunarTextPaint.style = Paint.Style.FILL
        selectedLunarTextPaint.textAlign = Paint.Align.CENTER

        selectedItemPaint.isAntiAlias = true
        selectedItemPaint.style = Paint.Style.FILL
        selectedItemPaint.strokeWidth = 2f

        curDayTextPaint.isAntiAlias = true
        curDayTextPaint.style = Paint.Style.FILL
        curDayTextPaint.textAlign = Paint.Align.CENTER
        curDayTextPaint.color = Color.RED
        curDayTextPaint.isFakeBoldText = true
        curDayTextPaint.textSize = CalendarUtil.dipToPx(context, TEXT_SIZE.toFloat()).toFloat()

        curDayLunarTextPaint.isAntiAlias = true
        curDayLunarTextPaint.style = Paint.Style.FILL
        curDayLunarTextPaint.textAlign = Paint.Align.CENTER
        curDayLunarTextPaint.color = Color.RED
        curDayLunarTextPaint.isFakeBoldText = true
        curDayLunarTextPaint.textSize = CalendarUtil.dipToPx(context, TEXT_SIZE.toFloat()).toFloat()

        setOnClickListener(this)
        setOnLongClickListener(this)
    }

    /**
     * 初始化所有UI配置
     */
    fun setup(delegate: CalendarViewDelegate) {
        viewDelegate = delegate
        updateStyle()
        updateItemHeight()
        initPaint()
    }

    fun updateStyle() {
        curMonthTextPaint.color = viewDelegate.currentMonthTextColor
        curMonthTextPaint.textSize = viewDelegate.dayTextSize.toFloat()

        curMonthLunarTextPaint.color = viewDelegate.currentMonthLunarTextColor
        curMonthLunarTextPaint.textSize = viewDelegate.lunarTextSize.toFloat()

        otherMonthTextPaint.color = viewDelegate.otherMonthTextColor
        otherMonthTextPaint.textSize = viewDelegate.dayTextSize.toFloat()

        otherMonthLunarTextPaint.color = viewDelegate.otherMonthLunarTextColor
        otherMonthLunarTextPaint.textSize = viewDelegate.lunarTextSize.toFloat()

        schemeTextPaint.color = viewDelegate.schemeTextColor
        schemeTextPaint.textSize = viewDelegate.dayTextSize.toFloat()

        schemeLunarTextPaint.color = viewDelegate.schemeLunarTextColor
        schemeLunarTextPaint.textSize = viewDelegate.lunarTextSize.toFloat()

        schemePaint.color = viewDelegate.schemeThemeColor

        selectTextPaint.color = viewDelegate.selectedTextColor
        selectTextPaint.textSize = viewDelegate.dayTextSize.toFloat()

        selectedLunarTextPaint.color = viewDelegate.selectedLunarTextColor
        selectedLunarTextPaint.textSize = viewDelegate.lunarTextSize.toFloat()

        selectedItemPaint.style = Paint.Style.FILL
        selectedItemPaint.color = viewDelegate.selectedThemeColor

        curDayTextPaint.color = viewDelegate.curDayTextColor
        curDayTextPaint.textSize = viewDelegate.dayTextSize.toFloat()

        curDayLunarTextPaint.color = viewDelegate.curDayLunarTextColor
        curDayLunarTextPaint.textSize = viewDelegate.lunarTextSize.toFloat()
    }

    open fun updateItemHeight() {
        itemHeight = viewDelegate.calendarItemHeight
        val metrics = curMonthTextPaint.fontMetrics
        textBaseLine = itemHeight / 2 - metrics.descent + (metrics.bottom - metrics.top) / 2
    }

    /**
     * 移除事件
     */
    fun removeSchemes() {
        for (a in items) {
            a.scheme = ""
            a.schemeColor = 0
            a.schemes = null
        }
    }

    /**
     * 添加事件标记，来自Map
     */
    fun addSchemesFromMap() {
        if (viewDelegate.schemeDatesMap.isEmpty()) {
            return
        }
        for (a in items) {
            if (viewDelegate.schemeDatesMap.containsKey(a.toString())) {
                val d = viewDelegate.schemeDatesMap[a.toString()] ?: continue
                a.scheme = if (d.scheme.isBlank()) viewDelegate.schemeText else d.scheme
                a.schemeColor = d.schemeColor
                a.schemes = d.schemes
            } else {
                a.scheme = ""
                a.schemeColor = 0
                a.schemes = null
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.pointerCount > 1) {
            return false
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mX = event.x
                mY = event.y
                isClick = true
            }
            MotionEvent.ACTION_MOVE -> {
                val mDY: Float
                if (isClick) {
                    mDY = event.y - mY
                    isClick = abs(mDY) <= 50
                }
            }
            MotionEvent.ACTION_UP -> {
                mX = event.x
                mY = event.y
            }
            else -> {
            }
        }
        return super.onTouchEvent(event)
    }

    /**
     * 开始绘制前的钩子，这里做一些初始化的操作，每次绘制只调用一次，性能高效
     * 没有需要可忽略不实现
     * 例如：
     * 1、需要绘制圆形标记事件背景，可以在这里计算半径
     * 2、绘制矩形选中效果，也可以在这里计算矩形宽和高
     */
    protected open fun onPreviewHook() {}

    /**
     * 是否是选中的
     */
    protected open fun isSelected(calendar: Calendar): Boolean {
        return items.indexOf(calendar) == currentItem
    }

    /**
     * 更新事件
     */
    fun update() {
        if (viewDelegate.schemeDatesMap.isEmpty()) { //清空操作
            removeSchemes()
            invalidate()
            return
        }
        addSchemesFromMap()
        invalidate()
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
     * 是否在日期范围内
     *
     * @return 是否在日期范围内
     */
    protected fun isInRange(calendar: Calendar): Boolean {
        return CalendarUtil.isCalendarInRange(calendar, viewDelegate)
    }

    /**
     * 跟新当前日期
     */
    abstract fun updateCurrentDate()

    /**
     * 销毁
     */
    abstract fun onDestroy()

    /**
     * 初始化画笔相关
     */
    protected open fun initPaint() {}
}