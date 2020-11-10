package com.tiamosu.calendarview.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import com.tiamosu.calendarview.delegate.CalendarViewDelegate
import com.tiamosu.calendarview.entity.Calendar
import com.tiamosu.calendarview.utils.CalendarUtil

/**
 * 年视图
 *
 * @author tiamosu
 * @date 2020/5/25.
 */
abstract class YearView @JvmOverloads constructor(
    context: Context?, attrs: AttributeSet? = null
) : View(context, attrs) {

    lateinit var viewDelegate: CalendarViewDelegate

    /**
     * 当前月份日期的笔
     */
    protected var curMonthTextPaint = Paint()

    /**
     * 其它月份日期颜色
     */
    protected var otherMonthTextPaint = Paint()

    /**
     * 当前月份农历文本颜色
     */
    protected var curMonthLunarTextPaint = Paint()

    /**
     * 当前月份农历文本颜色
     */
    protected var selectedLunarTextPaint = Paint()

    /**
     * 其它月份农历文本颜色
     */
    protected var otherMonthLunarTextPaint = Paint()

    /**
     * 其它月份农历文本颜色
     */
    protected var schemeLunarTextPaint = Paint()

    /**
     * 标记的日期背景颜色画笔
     */
    protected var schemePaint = Paint()

    /**
     * 被选择的日期背景色
     */
    protected var selectedPaint = Paint()

    /**
     * 标记的文本画笔
     */
    protected var schemeTextPaint = Paint()

    /**
     * 选中的文本画笔
     */
    protected var selectTextPaint = Paint()

    /**
     * 当前日期文本颜色画笔
     */
    protected var curDayTextPaint = Paint()

    /**
     * 当前日期文本颜色画笔
     */
    protected var curDayLunarTextPaint = Paint()

    /**
     * 月份画笔
     */
    protected var monthTextPaint = Paint()

    /**
     * 周栏画笔
     */
    protected var weekTextPaint = Paint()

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
     * Text的基线
     */
    protected var monthTextBaseLine = 0f

    /**
     * Text的基线
     */
    protected var weekTextBaseLine = 0f

    /**
     * 当前日历卡年份
     */
    protected var year = 0

    /**
     * 当前日历卡月份
     */
    protected var month = 0

    /**
     * 下个月偏移的数量
     */
    protected var nextDiff = 0

    /**
     * 周起始
     */
    protected var weekStart = 0

    /**
     * 日历的行数
     */
    protected var lineCount = 0

    init {
        initPaint()
    }

    /**
     * 初始化配置
     */
    private fun initPaint() {
        curMonthTextPaint.isAntiAlias = true
        curMonthTextPaint.textAlign = Paint.Align.CENTER
        curMonthTextPaint.color = -0xeeeeef
        curMonthTextPaint.isFakeBoldText = true

        otherMonthTextPaint.isAntiAlias = true
        otherMonthTextPaint.textAlign = Paint.Align.CENTER
        otherMonthTextPaint.color = -0x1e1e1f
        otherMonthTextPaint.isFakeBoldText = true

        curMonthLunarTextPaint.isAntiAlias = true
        curMonthLunarTextPaint.textAlign = Paint.Align.CENTER

        selectedLunarTextPaint.isAntiAlias = true
        selectedLunarTextPaint.textAlign = Paint.Align.CENTER

        otherMonthLunarTextPaint.isAntiAlias = true
        otherMonthLunarTextPaint.textAlign = Paint.Align.CENTER

        monthTextPaint.isAntiAlias = true
        monthTextPaint.isFakeBoldText = true

        weekTextPaint.isAntiAlias = true
        weekTextPaint.isFakeBoldText = true
        weekTextPaint.textAlign = Paint.Align.CENTER

        schemeLunarTextPaint.isAntiAlias = true
        schemeLunarTextPaint.textAlign = Paint.Align.CENTER

        schemeTextPaint.isAntiAlias = true
        schemeTextPaint.style = Paint.Style.FILL
        schemeTextPaint.textAlign = Paint.Align.CENTER
        schemeTextPaint.color = -0x12acad
        schemeTextPaint.isFakeBoldText = true

        selectTextPaint.isAntiAlias = true
        selectTextPaint.style = Paint.Style.FILL
        selectTextPaint.textAlign = Paint.Align.CENTER
        selectTextPaint.color = -0x12acad
        selectTextPaint.isFakeBoldText = true

        schemePaint.isAntiAlias = true
        schemePaint.style = Paint.Style.FILL
        schemePaint.strokeWidth = 2f
        schemePaint.color = -0x101011

        curDayTextPaint.isAntiAlias = true
        curDayTextPaint.textAlign = Paint.Align.CENTER
        curDayTextPaint.color = Color.RED
        curDayTextPaint.isFakeBoldText = true

        curDayLunarTextPaint.isAntiAlias = true
        curDayLunarTextPaint.textAlign = Paint.Align.CENTER
        curDayLunarTextPaint.color = Color.RED
        curDayLunarTextPaint.isFakeBoldText = true

        selectedPaint.isAntiAlias = true
        selectedPaint.style = Paint.Style.FILL
        selectedPaint.strokeWidth = 2f
    }

    /**
     * 设置
     *
     * @param delegate delegate
     */
    fun setup(delegate: CalendarViewDelegate) {
        viewDelegate = delegate
        updateStyle()
    }

    fun updateStyle() {
        curMonthTextPaint.textSize = viewDelegate.yearViewDayTextSize.toFloat()
        schemeTextPaint.textSize = viewDelegate.yearViewDayTextSize.toFloat()
        otherMonthTextPaint.textSize = viewDelegate.yearViewDayTextSize.toFloat()
        curDayTextPaint.textSize = viewDelegate.yearViewDayTextSize.toFloat()
        selectTextPaint.textSize = viewDelegate.yearViewDayTextSize.toFloat()
        schemeTextPaint.color = viewDelegate.yearViewSchemeTextColor
        curMonthTextPaint.color = viewDelegate.yearViewDayTextColor
        otherMonthTextPaint.color = viewDelegate.yearViewDayTextColor
        curDayTextPaint.color = viewDelegate.yearViewCurDayTextColor
        selectTextPaint.color = viewDelegate.yearViewSelectTextColor
        monthTextPaint.textSize = viewDelegate.yearViewMonthTextSize.toFloat()
        monthTextPaint.color = viewDelegate.yearViewMonthTextColor
        weekTextPaint.color = viewDelegate.yearViewWeekTextColor
        weekTextPaint.textSize = viewDelegate.yearViewWeekTextSize.toFloat()
    }

    /**
     * 初始化年视图
     *
     * @param year  year
     * @param month month
     */
    fun init(year: Int, month: Int) {
        this.year = year
        this.month = month
        nextDiff = CalendarUtil.getMonthEndDiff(this.year, this.month, viewDelegate.weekStart)
        items = CalendarUtil.initCalendarForMonthView(
            this.year,
            this.month,
            viewDelegate.currentDay,
            viewDelegate.weekStart
        )
        lineCount = 6
        addSchemesFromMap()
    }

    /**
     * 测量大小
     *
     * @param width  width
     * @param height height
     */
    fun measureSize(width: Int, height: Int) {
        val rect = Rect()
        curMonthTextPaint.getTextBounds("1", 0, 1, rect)
        val textHeight = rect.height()
        val mMinHeight = 12 * textHeight + monthViewTop
        val h = if (height >= mMinHeight) height else mMinHeight
        layoutParams.width = width
        layoutParams.height = h
        itemHeight = (h - monthViewTop) / 6
        val metrics = curMonthTextPaint.fontMetrics
        textBaseLine = itemHeight / 2 - metrics.descent + (metrics.bottom - metrics.top) / 2
        val monthMetrics = monthTextPaint.fontMetrics
        monthTextBaseLine = viewDelegate.yearViewMonthHeight / 2 - monthMetrics.descent +
                (monthMetrics.bottom - monthMetrics.top) / 2
        val weekMetrics = weekTextPaint.fontMetrics
        weekTextBaseLine = viewDelegate.yearViewWeekHeight / 2 - weekMetrics.descent +
                (weekMetrics.bottom - weekMetrics.top) / 2
        invalidate()
    }

    /**
     * 添加事件标记，来自Map
     */
    private fun addSchemesFromMap() {
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

    override fun onDraw(canvas: Canvas) {
        itemWidth =
            (width - viewDelegate.yearViewMonthPaddingLeft - viewDelegate.yearViewMonthPaddingRight) / 7
        onPreviewHook()
        onDrawMonth(canvas)
        onDrawWeek(canvas)
        onDrawMonthView(canvas)
    }

    /**
     * 绘制
     *
     * @param canvas canvas
     */
    private fun onDrawMonth(canvas: Canvas) {
        onDrawMonth(
            canvas,
            year, month,
            viewDelegate.yearViewMonthPaddingLeft,
            viewDelegate.yearViewMonthPaddingTop,
            width - 2 * viewDelegate.yearViewMonthPaddingRight,
            viewDelegate.yearViewMonthHeight + viewDelegate.yearViewMonthPaddingTop
        )
    }

    private val monthViewTop: Int
        get() = viewDelegate.yearViewMonthPaddingTop +
                viewDelegate.yearViewMonthHeight +
                viewDelegate.yearViewMonthPaddingBottom +
                viewDelegate.yearViewWeekHeight

    /**
     * 绘制
     */
    private fun onDrawWeek(canvas: Canvas) {
        if (viewDelegate.yearViewWeekHeight <= 0) {
            return
        }
        var week = viewDelegate.weekStart
        if (week > 0) {
            week -= 1
        }
        val width =
            (width - viewDelegate.yearViewMonthPaddingLeft - viewDelegate.yearViewMonthPaddingRight) / 7

        for (i in 0..6) {
            onDrawWeek(
                canvas,
                week,
                viewDelegate.yearViewMonthPaddingLeft + i * width,
                viewDelegate.yearViewMonthHeight + viewDelegate.yearViewMonthPaddingTop + viewDelegate.yearViewMonthPaddingBottom,
                width,
                viewDelegate.yearViewWeekHeight
            )

            week += 1
            if (week >= 7) {
                week = 0
            }
        }
    }

    /**
     * 绘制月份数据
     *
     * @param canvas canvas
     */
    private fun onDrawMonthView(canvas: Canvas) {
        var d = 0
        for (i in 0 until lineCount) {
            for (j in 0..6) {
                val calendar = items[d]
                if (d > items.size - nextDiff) {
                    return
                }
                if (!calendar.isCurrentMonth) {
                    ++d
                    continue
                }
                draw(canvas, calendar, i, j, d)
                ++d
            }
        }
    }

    /**
     * 开始绘制
     *
     * @param calendar 对应日历
     */
    @Suppress("UNUSED_PARAMETER")
    private fun draw(canvas: Canvas, calendar: Calendar, i: Int, j: Int, d: Int) {
        val x = j * itemWidth + viewDelegate.yearViewMonthPaddingLeft
        val y = i * itemHeight + monthViewTop
        val isSelected = calendar == viewDelegate.selectedCalendar
        val hasScheme = calendar.hasScheme()
        if (hasScheme) {
            //标记的日子
            var isDrawSelected = false //是否继续绘制选中的onDrawScheme
            if (isSelected) {
                isDrawSelected = onDrawSelected(canvas, calendar, x, y, true)
            }
            if (isDrawSelected || !isSelected) {
                //将画笔设置为标记颜色
                schemePaint.color =
                    if (calendar.schemeColor != 0) calendar.schemeColor else viewDelegate.schemeThemeColor
                onDrawScheme(canvas, calendar, x, y)
            }
        } else {
            if (isSelected) {
                onDrawSelected(canvas, calendar, x, y, false)
            }
        }
        onDrawText(canvas, calendar, x, y, hasScheme, isSelected)
    }

    /**
     * 开始绘制前的钩子，这里做一些初始化的操作，每次绘制只调用一次，性能高效
     * 没有需要可忽略不实现
     * 例如：
     * 1、需要绘制圆形标记事件背景，可以在这里计算半径
     * 2、绘制矩形选中效果，也可以在这里计算矩形宽和高
     */
    protected fun onPreviewHook() {
    }

    /**
     * 绘制月份
     *
     * @param canvas canvas
     * @param year   year
     * @param month  month
     * @param x      x
     * @param y      y
     * @param width  width
     * @param height height
     */
    protected abstract fun onDrawMonth(
        canvas: Canvas,
        year: Int,
        month: Int,
        x: Int,
        y: Int,
        width: Int,
        height: Int
    )

    /**
     * 绘制年视图的周栏
     *
     * @param canvas canvas
     * @param week   week
     * @param x      x
     * @param y      y
     * @param width  width
     * @param height height
     */
    protected abstract fun onDrawWeek(
        canvas: Canvas,
        week: Int,
        x: Int,
        y: Int,
        width: Int,
        height: Int
    )

    /**
     * 绘制选中的日期
     *
     * @param canvas    canvas
     * @param calendar  日历日历calendar
     * @param x         日历Card x起点坐标
     * @param y         日历Card y起点坐标
     * @param hasScheme hasScheme 非标记的日期
     * @return 是否绘制onDrawScheme，true or false
     */
    protected abstract fun onDrawSelected(
        canvas: Canvas,
        calendar: Calendar,
        x: Int,
        y: Int,
        hasScheme: Boolean
    ): Boolean

    /**
     * 绘制标记的日期,这里可以是背景色，标记色什么的
     *
     * @param canvas   canvas
     * @param calendar 日历calendar
     * @param x        日历Card x起点坐标
     * @param y        日历Card y起点坐标
     */
    protected abstract fun onDrawScheme(canvas: Canvas, calendar: Calendar, x: Int, y: Int)

    /**
     * 绘制日历文本
     *
     * @param canvas     canvas
     * @param calendar   日历calendar
     * @param x          日历Card x起点坐标
     * @param y          日历Card y起点坐标
     * @param hasScheme  是否是标记的日期
     * @param isSelected 是否选中
     */
    protected abstract fun onDrawText(
        canvas: Canvas,
        calendar: Calendar,
        x: Int,
        y: Int,
        hasScheme: Boolean,
        isSelected: Boolean
    )
}