package com.tiamosu.calendarview.delegate

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import com.tiamosu.calendarview.CalendarView
import com.tiamosu.calendarview.R
import com.tiamosu.calendarview.entity.Calendar
import com.tiamosu.calendarview.utils.CalendarUtil
import com.tiamosu.calendarview.utils.LunarCalendar
import com.tiamosu.calendarview.view.DefaultMonthView
import com.tiamosu.calendarview.view.DefaultWeekView
import com.tiamosu.calendarview.view.DefaultYearView
import com.tiamosu.calendarview.view.WeekBar
import java.util.*

/**
 * 属性委托
 *
 * @author tiamosu
 * @date 2020/5/25.
 */
class CalendarViewDelegate constructor(context: Context, attrs: AttributeSet?) {

    var defaultCalendarSelectDay: Int

    /**
     * 周起始
     */
    var weekStart: Int

    /**
     * 月份显示模式
     */
    var monthViewShowMode: Int

    /**
     * 选择模式
     */
    var selectMode: Int

    /**
     * 各种字体颜色，看名字知道对应的地方
     */
    var curDayTextColor: Int
    val curDayLunarTextColor: Int
    val weekTextColor: Int
    var schemeTextColor: Int
    var schemeLunarTextColor: Int
    var otherMonthTextColor: Int
    var currentMonthTextColor: Int
    var selectedTextColor: Int
    var selectedLunarTextColor: Int
    var currentMonthLunarTextColor: Int
    var otherMonthLunarTextColor: Int

    var isPreventLongPressedSelected = false

    /**
     * 年视图一些padding
     */
    var yearViewPadding = 0

    /**
     * 年视图一些padding
     */
    var yearViewPaddingLeft = 0

    /**
     * 年视图一些padding
     */
    var yearViewPaddingRight = 0

    /**
     * 年视图一些padding
     */
    var yearViewMonthPaddingLeft = 0

    /**
     * 年视图一些padding
     */
    var yearViewMonthPaddingRight = 0

    /**
     * 年视图一些padding
     */
    var yearViewMonthPaddingTop = 0

    /**
     * 年视图一些padding
     */
    var yearViewMonthPaddingBottom = 0

    /**
     * 日历内部左padding
     */
    var calendarPaddingLeft = 0
        private set

    /**
     * 日历内部右padding
     */
    var calendarPaddingRight = 0
        private set

    /**
     * 日历内部左右padding
     */
    var calendarPadding: Int
        private set

    /**
     * 年视图字体大小
     */
    val yearViewMonthTextSize: Int
    val yearViewDayTextSize: Int
    val yearViewWeekTextSize: Int

    /**
     * 年视图月份高度和周的高度
     */
    val yearViewMonthHeight: Int
    val yearViewWeekHeight: Int

    /**
     * 年视图字体和标记颜色
     */
    var yearViewMonthTextColor: Int
        private set
    var yearViewDayTextColor: Int
        private set
    var yearViewSchemeTextColor: Int
        private set
    val yearViewSelectTextColor: Int
    val yearViewCurDayTextColor: Int
    val yearViewWeekTextColor: Int

    /**
     * 星期栏的背景、线的背景、年份背景
     */
    val weekLineBackground: Int
    val yearViewBackground: Int
    val weekBackground: Int

    /**
     * 星期栏Line margin
     */
    val weekLineMargin: Int

    /**
     * 星期栏字体大小
     */
    val weekTextSize: Int

    /**
     * 标记的主题色和选中的主题色
     */
    var schemeThemeColor: Int
        private set
    var selectedThemeColor: Int
        private set

    /**
     * 选中的主题视图圆角大小
     */
    var selectThemeRadius: Float

    /**
     * 是否展示农历
     */
    var isLunarTextVisible: Boolean

    /**
     * 自定义的日历路径
     */
    private val monthViewClassPath: String?

    /**
     * 月视图类
     */
    var monthViewClass: Class<*>? = null

    /**
     * 自定义周视图路径
     */
    private val weekViewClassPath: String?

    /**
     * 周视图类
     */
    var weekViewClass: Class<*>? = null

    /**
     * 自定义年视图路径
     */
    val yearViewClassPath: String?

    /**
     * 周视图类
     */
    var yearViewClass: Class<*>? = null
        private set

    /**
     * 自定义周栏路径
     */
    private val weekBarClassPath: String?

    /**
     * 自定义周栏
     */
    lateinit var weekBarClass: Class<*>

    /**
     * 年月视图是否打开
     */
    var isShowYearSelectedLayout = false

    /**
     * 标记文本
     */
    var schemeText = ""

    /**
     * 最小年份和最大年份
     */
    var minYear: Int
        private set
    var maxYear: Int
        private set

    /**
     * 最小年份和最大年份对应最小月份和最大月份
     * when you want set 2015-07 to 2017-08
     */
    var minYearMonth: Int
        private set
    var maxYearMonth: Int
        private set

    /**
     * 最小年份和最大年份对应最小天和最大天数
     * when you want set like 2015-07-08 to 2017-08-30
     */
    var minYearDay: Int
        private set
    var maxYearDay: Int
        private set

    /**
     * 日期和农历文本大小
     */
    val dayTextSize: Int
    val lunarTextSize: Int

    /**
     * 日历卡的项高度
     */
    var calendarItemHeight: Int

    /**
     * 是否是全屏日历
     */
    val isFullScreenCalendar: Boolean

    /**
     * 星期栏的高度
     */
    val weekBarHeight: Int

    /**
     * 今天的日子
     */
    lateinit var currentDay: Calendar
        private set
    var isMonthViewScrollable: Boolean
    var isWeekViewScrollable: Boolean
    var isYearViewScrollable: Boolean

    /**
     * 当前月份和周视图的item位置
     */
    var currentMonthViewItem = 0

    /**
     * 标记的日期,数量巨大，请使用这个
     */
    var schemeDatesMap: MutableMap<String, Calendar> = hashMapOf()

    /**
     * 点击Padding位置事件
     */
    var clickCalendarPaddingListener: CalendarView.OnClickCalendarPaddingListener? = null

    /**
     * 日期拦截事件
     */
    var calendarInterceptListener: CalendarView.OnCalendarInterceptListener? = null

    /**
     * 日期选中监听
     */
    var calendarSelectListener: CalendarView.OnCalendarSelectListener? = null

    /**
     * 范围选择
     */
    var calendarRangeSelectListener: CalendarView.OnCalendarRangeSelectListener? = null

    /**
     * 多选选择事件
     */
    var calendarMultiSelectListener: CalendarView.OnCalendarMultiSelectListener? = null

    /**
     * 外部日期长按事件
     */
    var calendarLongClickListener: CalendarView.OnCalendarLongClickListener? = null

    /**
     * 内部日期切换监听，用于内部更新计算
     */
    var innerDateSelectedListener: CalendarView.OnInnerDateSelectedListener? = null

    /**
     * 快速年份切换
     */
    var yearChangeListener: CalendarView.OnYearChangeListener? = null

    /**
     * 月份切换事件
     */
    var monthChangeListener: CalendarView.OnMonthChangeListener? = null

    /**
     * 周视图改变事件
     */
    var weekChangeListener: CalendarView.OnWeekChangeListener? = null

    /**
     * 视图改变事件
     */
    var viewChangeListener: CalendarView.OnViewChangeListener? = null

    /**
     * 年视图改变事件
     */
    var yearViewChangeListener: CalendarView.OnYearViewChangeListener? = null

    /**
     * 保存选中的日期
     */
    var selectedCalendar = Calendar()

    /**
     * 保存标记位置
     */
    var indexCalendar = Calendar()

    /**
     * 多选日历
     */
    var selectedCalendars: MutableMap<String, Calendar> = HashMap()
    var maxMultiSelectSize: Int

    /**
     * 选择范围日历
     */
    var selectedStartRangeCalendar: Calendar? = null
    var selectedEndRangeCalendar: Calendar? = null
    var minSelectRange: Int
        private set
    var maxSelectRange: Int
        private set

    init {
        val array = context.obtainStyledAttributes(attrs, R.styleable.CalendarView)
        LunarCalendar.init(context)
        calendarPadding = array.getDimension(R.styleable.CalendarView_calendar_padding, 0f).toInt()
        calendarPaddingLeft =
            array.getDimension(R.styleable.CalendarView_calendar_padding_left, 0f).toInt()
        calendarPaddingRight =
            array.getDimension(R.styleable.CalendarView_calendar_padding_right, 0f).toInt()

        if (calendarPadding != 0) {
            calendarPaddingLeft = calendarPadding
            calendarPaddingRight = calendarPadding
        }

        schemeTextColor = array.getColor(R.styleable.CalendarView_scheme_text_color, -0x1)
        schemeLunarTextColor =
            array.getColor(R.styleable.CalendarView_scheme_lunar_text_color, -0x1e1e1f)
        schemeThemeColor = array.getColor(R.styleable.CalendarView_scheme_theme_color, 0x50CFCFCF)
        monthViewClassPath = array.getString(R.styleable.CalendarView_month_view)
        yearViewClassPath = array.getString(R.styleable.CalendarView_year_view)
        weekViewClassPath = array.getString(R.styleable.CalendarView_week_view)
        weekBarClassPath = array.getString(R.styleable.CalendarView_week_bar_view)
        weekTextSize = array.getDimensionPixelSize(
            R.styleable.CalendarView_week_text_size,
            CalendarUtil.dipToPx(context, 12f)
        )
        weekBarHeight = array.getDimension(
            R.styleable.CalendarView_week_bar_height,
            CalendarUtil.dipToPx(context, 40f).toFloat()
        ).toInt()
        weekLineMargin = array.getDimension(
            R.styleable.CalendarView_week_line_margin,
            CalendarUtil.dipToPx(context, 0f).toFloat()
        ).toInt()
        schemeText = array.getString(R.styleable.CalendarView_scheme_text) ?: ""

        if (schemeText.isBlank()) {
            schemeText = "记"
        }
        isMonthViewScrollable =
            array.getBoolean(R.styleable.CalendarView_month_view_scrollable, true)
        isWeekViewScrollable = array.getBoolean(R.styleable.CalendarView_week_view_scrollable, true)
        isYearViewScrollable = array.getBoolean(R.styleable.CalendarView_year_view_scrollable, true)
        defaultCalendarSelectDay = array.getInt(
            R.styleable.CalendarView_month_view_auto_select_day,
            FIRST_DAY_OF_MONTH
        )
        monthViewShowMode =
            array.getInt(R.styleable.CalendarView_month_view_show_mode, MODE_ALL_MONTH)
        weekStart = array.getInt(R.styleable.CalendarView_week_start_with, WEEK_START_WITH_SUN)
        selectMode = array.getInt(R.styleable.CalendarView_select_mode, SELECT_MODE_DEFAULT)
        maxMultiSelectSize =
            array.getInt(R.styleable.CalendarView_max_multi_select_size, Int.MAX_VALUE)
        minSelectRange = array.getInt(R.styleable.CalendarView_min_select_range, -1)
        maxSelectRange = array.getInt(R.styleable.CalendarView_max_select_range, -1)
        setSelectRange(minSelectRange, maxSelectRange)
        weekBackground = array.getColor(R.styleable.CalendarView_week_background, Color.WHITE)
        weekLineBackground =
            array.getColor(R.styleable.CalendarView_week_line_background, Color.TRANSPARENT)
        yearViewBackground =
            array.getColor(R.styleable.CalendarView_year_view_background, Color.WHITE)
        weekTextColor = array.getColor(R.styleable.CalendarView_week_text_color, -0xcccccd)
        curDayTextColor = array.getColor(R.styleable.CalendarView_current_day_text_color, Color.RED)
        curDayLunarTextColor =
            array.getColor(R.styleable.CalendarView_current_day_lunar_text_color, Color.RED)
        selectedThemeColor =
            array.getColor(R.styleable.CalendarView_selected_theme_color, 0x50CFCFCF)

        selectThemeRadius = array.getDimension(R.styleable.CalendarView_selected_theme_radius, 0f)
        isLunarTextVisible = array.getBoolean(R.styleable.CalendarView_lunar_text_visible, true)

        selectedTextColor = array.getColor(R.styleable.CalendarView_selected_text_color, -0xeeeeef)
        selectedLunarTextColor =
            array.getColor(R.styleable.CalendarView_selected_lunar_text_color, -0xeeeeef)
        currentMonthTextColor =
            array.getColor(R.styleable.CalendarView_current_month_text_color, -0xeeeeef)
        otherMonthTextColor =
            array.getColor(R.styleable.CalendarView_other_month_text_color, -0x1e1e1f)
        currentMonthLunarTextColor =
            array.getColor(R.styleable.CalendarView_current_month_lunar_text_color, -0x1e1e1f)
        otherMonthLunarTextColor =
            array.getColor(R.styleable.CalendarView_other_month_lunar_text_color, -0x1e1e1f)
        minYear = array.getInt(R.styleable.CalendarView_min_year, 1971)
        maxYear = array.getInt(R.styleable.CalendarView_max_year, 2055)
        minYearMonth = array.getInt(R.styleable.CalendarView_min_year_month, 1)
        maxYearMonth = array.getInt(R.styleable.CalendarView_max_year_month, 12)
        minYearDay = array.getInt(R.styleable.CalendarView_min_year_day, 1)
        maxYearDay = array.getInt(R.styleable.CalendarView_max_year_day, -1)
        dayTextSize = array.getDimensionPixelSize(
            R.styleable.CalendarView_day_text_size,
            CalendarUtil.dipToPx(context, 16f)
        )
        lunarTextSize = array.getDimensionPixelSize(
            R.styleable.CalendarView_lunar_text_size,
            CalendarUtil.dipToPx(context, 10f)
        )
        calendarItemHeight = array.getDimension(
            R.styleable.CalendarView_calendar_height,
            CalendarUtil.dipToPx(context, 56f).toFloat()
        ).toInt()
        isFullScreenCalendar =
            array.getBoolean(R.styleable.CalendarView_calendar_match_parent, false)

        //年视图相关
        yearViewMonthTextSize = array.getDimensionPixelSize(
            R.styleable.CalendarView_year_view_month_text_size,
            CalendarUtil.dipToPx(context, 18f)
        )
        yearViewDayTextSize = array.getDimensionPixelSize(
            R.styleable.CalendarView_year_view_day_text_size,
            CalendarUtil.dipToPx(context, 7f)
        )
        yearViewMonthTextColor =
            array.getColor(R.styleable.CalendarView_year_view_month_text_color, -0xeeeeef)
        yearViewDayTextColor =
            array.getColor(R.styleable.CalendarView_year_view_day_text_color, -0xeeeeef)
        yearViewSchemeTextColor =
            array.getColor(R.styleable.CalendarView_year_view_scheme_color, schemeThemeColor)
        yearViewWeekTextColor =
            array.getColor(R.styleable.CalendarView_year_view_week_text_color, -0xcccccd)
        yearViewCurDayTextColor = array.getColor(
            R.styleable.CalendarView_year_view_current_day_text_color,
            curDayTextColor
        )
        yearViewSelectTextColor =
            array.getColor(R.styleable.CalendarView_year_view_select_text_color, -0xcccccd)
        yearViewWeekTextSize = array.getDimensionPixelSize(
            R.styleable.CalendarView_year_view_week_text_size,
            CalendarUtil.dipToPx(context, 8f)
        )
        yearViewMonthHeight = array.getDimensionPixelSize(
            R.styleable.CalendarView_year_view_month_height,
            CalendarUtil.dipToPx(context, 32f)
        )
        yearViewWeekHeight = array.getDimensionPixelSize(
            R.styleable.CalendarView_year_view_week_height,
            CalendarUtil.dipToPx(context, 0f)
        )
        yearViewPadding = array.getDimension(
            R.styleable.CalendarView_year_view_padding,
            CalendarUtil.dipToPx(context, 12f).toFloat()
        ).toInt()
        yearViewPaddingLeft = array.getDimension(
            R.styleable.CalendarView_year_view_padding_left,
            CalendarUtil.dipToPx(context, 12f).toFloat()
        ).toInt()
        yearViewPaddingRight = array.getDimension(
            R.styleable.CalendarView_year_view_padding_right,
            CalendarUtil.dipToPx(context, 12f).toFloat()
        ).toInt()

        if (yearViewPadding != 0) {
            yearViewPaddingLeft = yearViewPadding
            yearViewPaddingRight = yearViewPadding
        }

        yearViewMonthPaddingTop = array.getDimension(
            R.styleable.CalendarView_year_view_month_padding_top,
            CalendarUtil.dipToPx(context, 4f).toFloat()
        ).toInt()
        yearViewMonthPaddingBottom = array.getDimension(
            R.styleable.CalendarView_year_view_month_padding_bottom,
            CalendarUtil.dipToPx(context, 4f).toFloat()
        ).toInt()
        yearViewMonthPaddingLeft = array.getDimension(
            R.styleable.CalendarView_year_view_month_padding_left,
            CalendarUtil.dipToPx(context, 4f).toFloat()
        ).toInt()
        yearViewMonthPaddingRight = array.getDimension(
            R.styleable.CalendarView_year_view_month_padding_right,
            CalendarUtil.dipToPx(context, 4f).toFloat()
        ).toInt()

        if (minYear <= MIN_YEAR) {
            minYear = MIN_YEAR
        }
        if (maxYear >= MAX_YEAR) {
            maxYear = MAX_YEAR
        }
        array.recycle()

        init()
    }

    private fun init() {
        currentDay = Calendar()
        val d = Date()
        currentDay.year = CalendarUtil.getDate("yyyy", d)
        currentDay.month = CalendarUtil.getDate("MM", d)
        currentDay.day = CalendarUtil.getDate("dd", d)
        currentDay.isCurrentDay = true
        LunarCalendar.setupLunarCalendar(currentDay)
        setRange(minYear, minYearMonth, maxYear, maxYearMonth)

        try {
            weekBarClass = if (weekBarClassPath.isNullOrBlank())
                WeekBar::class.java else Class.forName(weekBarClassPath)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            yearViewClass = if (yearViewClassPath.isNullOrBlank())
                DefaultYearView::class.java else Class.forName(yearViewClassPath)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            monthViewClass = if (monthViewClassPath.isNullOrBlank())
                DefaultMonthView::class.java else Class.forName(monthViewClassPath)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            weekViewClass = if (weekViewClassPath.isNullOrBlank())
                DefaultWeekView::class.java else Class.forName(weekViewClassPath)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setRange(
        minYear: Int, minYearMonth: Int,
        maxYear: Int, maxYearMonth: Int
    ) {
        this.minYear = minYear
        this.minYearMonth = minYearMonth
        this.maxYear = maxYear
        this.maxYearMonth = maxYearMonth
        if (this.maxYear < currentDay.year) {
            this.maxYear = currentDay.year
        }
        if (maxYearDay == -1) {
            maxYearDay = CalendarUtil.getMonthDaysCount(this.maxYear, this.maxYearMonth)
        }
        val y = currentDay.year - this.minYear
        currentMonthViewItem = 12 * y + currentDay.month - this.minYearMonth
    }

    fun setRange(
        minYear: Int, minYearMonth: Int, minYearDay: Int,
        maxYear: Int, maxYearMonth: Int, maxYearDay: Int
    ) {
        this.minYear = minYear
        this.minYearMonth = minYearMonth
        this.minYearDay = minYearDay
        this.maxYear = maxYear
        this.maxYearMonth = maxYearMonth
        this.maxYearDay = maxYearDay

        if (this.maxYearDay == -1) {
            this.maxYearDay = CalendarUtil.getMonthDaysCount(this.maxYear, this.maxYearMonth)
        }
        val y = currentDay.year - this.minYear
        currentMonthViewItem = 12 * y + currentDay.month - this.minYearMonth
    }

    fun setTextColor(
        curDayTextColor: Int, curMonthTextColor: Int, otherMonthTextColor: Int,
        curMonthLunarTextColor: Int, otherMonthLunarTextColor: Int
    ) {
        this.curDayTextColor = curDayTextColor
        this.otherMonthTextColor = otherMonthTextColor
        currentMonthTextColor = curMonthTextColor
        currentMonthLunarTextColor = curMonthLunarTextColor
        this.otherMonthLunarTextColor = otherMonthLunarTextColor
    }

    fun setSchemeColor(schemeColor: Int, schemeTextColor: Int, schemeLunarTextColor: Int) {
        schemeThemeColor = schemeColor
        this.schemeTextColor = schemeTextColor
        this.schemeLunarTextColor = schemeLunarTextColor
    }

    fun setYearViewTextColor(
        yearViewMonthTextColor: Int,
        yearViewDayTextColor: Int,
        yarViewSchemeTextColor: Int
    ) {
        this.yearViewMonthTextColor = yearViewMonthTextColor
        this.yearViewDayTextColor = yearViewDayTextColor
        yearViewSchemeTextColor = yarViewSchemeTextColor
    }

    fun setSelectColor(selectedColor: Int, selectedTextColor: Int, selectedLunarTextColor: Int) {
        selectedThemeColor = selectedColor
        this.selectedTextColor = selectedTextColor
        this.selectedLunarTextColor = selectedLunarTextColor
    }

    fun setThemeColor(selectedThemeColor: Int, schemeColor: Int) {
        this.selectedThemeColor = selectedThemeColor
        schemeThemeColor = schemeColor
    }

    fun setSelectRange(minRange: Int, maxRange: Int) {
        if (maxRange in 1 until minRange) {
            maxSelectRange = minRange
            minSelectRange = minRange
            return
        }
        minSelectRange = if (minRange <= 0) -1 else minRange
        maxSelectRange = if (maxRange <= 0) -1 else maxRange
    }

    fun updateCurrentDay() {
        val d = Date()
        currentDay.year = CalendarUtil.getDate("yyyy", d)
        currentDay.month = CalendarUtil.getDate("MM", d)
        currentDay.day = CalendarUtil.getDate("dd", d)
        LunarCalendar.setupLunarCalendar(currentDay)
    }

    fun clearSelectedScheme() {
        selectedCalendar.clearScheme()
    }

    fun updateSelectCalendarScheme() {
        if (schemeDatesMap.isNotEmpty()) {
            val key = selectedCalendar.toString()
            if (schemeDatesMap.containsKey(key)) {
                val d = schemeDatesMap[key]
                selectedCalendar.mergeScheme(d, schemeText)
            }
        } else {
            clearSelectedScheme()
        }
    }

    fun updateCalendarScheme(targetCalendar: Calendar?) {
        if (targetCalendar == null || schemeDatesMap.isEmpty()) {
            return
        }
        val key = targetCalendar.toString()
        if (schemeDatesMap.containsKey(key)) {
            val d = schemeDatesMap[key]
            targetCalendar.mergeScheme(d, schemeText)
        }
    }

    fun createCurrentDate(): Calendar {
        val calendar = Calendar().apply {
            year = currentDay.year
            week = currentDay.week
            month = currentDay.month
            day = currentDay.day
            isCurrentDay = true
        }
        LunarCalendar.setupLunarCalendar(calendar)
        return calendar
    }

    val minRangeCalendar: Calendar
        get() {
            val calendar = Calendar().apply {
                year = minYear
                month = minYearMonth
                day = minYearDay
            }
            calendar.isCurrentDay = calendar == currentDay
            LunarCalendar.setupLunarCalendar(calendar)
            return calendar
        }

    val maxRangeCalendar: Calendar
        get() {
            val calendar = Calendar().apply {
                year = maxYear
                month = maxYearMonth
                day = maxYearDay
            }
            calendar.isCurrentDay = calendar == currentDay
            LunarCalendar.setupLunarCalendar(calendar)
            return calendar
        }

    /**
     * 添加事件标记，来自Map
     */
    fun addSchemesFromMap(mItems: List<Calendar>) {
        if (schemeDatesMap.isEmpty()) {
            return
        }
        for (a in mItems) {
            if (schemeDatesMap.containsKey(a.toString())) {
                val d = schemeDatesMap[a.toString()] ?: continue
                a.scheme = if (d.scheme.isBlank()) schemeText else d.scheme
                a.schemeColor = d.schemeColor
                a.schemes = d.schemes
            } else {
                a.scheme = ""
                a.schemeColor = 0
                a.schemes = null
            }
        }
    }

    /**
     * 添加数据
     */
    fun addSchemes(mSchemeDates: Map<String, Calendar>) {
        if (mSchemeDates.isEmpty()) {
            return
        }
        for (key in mSchemeDates.keys) {
            schemeDatesMap.remove(key)
            val calendar = mSchemeDates[key] ?: continue
            schemeDatesMap[key] = calendar
        }
    }

    /**
     * 清楚选择
     */
    fun clearSelectRange() {
        selectedStartRangeCalendar = null
        selectedEndRangeCalendar = null
    }

    fun setCalendarPadding(calendarPadding: Int) {
        this.calendarPadding = calendarPadding
        calendarPaddingLeft = calendarPadding
        calendarPaddingRight = calendarPadding
    }

    fun setCalendarPaddingLeft(calendarPaddingLeft: Int) {
        this.calendarPaddingLeft = calendarPaddingLeft
    }

    fun setCalendarPaddingRight(calendarPaddingRight: Int) {
        this.calendarPaddingRight = calendarPaddingRight
    }

    /**
     * 获得选中范围
     *
     * @return 选中范围
     */
    val selectCalendarRange: List<Calendar>?
        get() {
            if (selectMode != SELECT_MODE_RANGE) {
                return null
            }
            val calendars: MutableList<Calendar> = ArrayList()
            if (selectedStartRangeCalendar == null ||
                selectedEndRangeCalendar == null
            ) {
                return calendars
            }
            val oneDay = 1000 * 3600 * 24.toLong()
            val date = java.util.Calendar.getInstance()
            date[selectedStartRangeCalendar!!.year, selectedStartRangeCalendar!!.month - 1] =
                selectedStartRangeCalendar!!.day
            val startTimeMills = date.timeInMillis //获得起始时间戳
            date[selectedEndRangeCalendar!!.year, selectedEndRangeCalendar!!.month - 1] =
                selectedEndRangeCalendar!!.day
            val endTimeMills = date.timeInMillis
            var start = startTimeMills
            while (start <= endTimeMills) {
                date.timeInMillis = start
                val calendar = Calendar().apply {
                    year = date[java.util.Calendar.YEAR]
                    month = date[java.util.Calendar.MONTH] + 1
                    day = date[java.util.Calendar.DAY_OF_MONTH]
                }
                LunarCalendar.setupLunarCalendar(calendar)
                updateCalendarScheme(calendar)

                if (calendarInterceptListener?.onCalendarIntercept(calendar) == true) {
                    start += oneDay
                    continue
                }
                calendars.add(calendar)
                start += oneDay
            }
            addSchemesFromMap(calendars)
            return calendars
        }

    companion object {
        /**
         * 周起始：周日
         */
        const val WEEK_START_WITH_SUN = 1

        /**
         * 周起始：周一
         */
        const val WEEK_START_WITH_MON = 2

        /**
         * 周起始：周六
         */
        const val WEEK_START_WITH_SAT = 7

        /**
         * 默认选择日期1号first_day_of_month
         */
        const val FIRST_DAY_OF_MONTH = 0

        /**
         * 跟随上个月last_select_day
         */
        const val LAST_MONTH_VIEW_SELECT_DAY = 1

        /**
         * 跟随上个月last_select_day_ignore_current忽视今天
         */
        const val LAST_MONTH_VIEW_SELECT_DAY_IGNORE_CURRENT = 2

        /**
         * 全部显示
         */
        const val MODE_ALL_MONTH = 0

        /**
         * 仅显示当前月份
         */
        const val MODE_ONLY_CURRENT_MONTH = 1

        /**
         * 自适应显示，不会多出一行，但是会自动填充
         */
        const val MODE_FIT_MONTH = 2

        /**
         * 默认选择模式
         */
        const val SELECT_MODE_DEFAULT = 0

        /**
         * 单选模式
         */
        const val SELECT_MODE_SINGLE = 1

        /**
         * 范围选择模式
         */
        const val SELECT_MODE_RANGE = 2

        /**
         * 多选模式
         */
        const val SELECT_MODE_MULTI = 3

        /**
         * 支持转换的最小农历年份
         */
        const val MIN_YEAR = 1900

        /**
         * 支持转换的最大农历年份
         */
        const val MAX_YEAR = 2099
    }
}