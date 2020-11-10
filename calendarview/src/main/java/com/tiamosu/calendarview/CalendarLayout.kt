package com.tiamosu.calendarview

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.*
import android.view.animation.LinearInterpolator
import android.widget.AbsListView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.RecyclerView
import com.tiamosu.calendarview.delegate.CalendarViewDelegate
import com.tiamosu.calendarview.entity.Calendar
import com.tiamosu.calendarview.utils.CalendarUtil
import com.tiamosu.calendarview.view.MonthViewPager
import com.tiamosu.calendarview.view.WeekBar
import com.tiamosu.calendarview.view.WeekViewPager
import com.tiamosu.calendarview.view.YearViewPager
import kotlin.math.abs

/**
 * 日历布局
 *
 * @author tiamosu
 * @date 2020/5/25.
 */
@Suppress("unused")
class CalendarLayout(context: Context, attrs: AttributeSet?) : LinearLayoutCompat(context, attrs) {

    /**
     * 多点触控支持
     */
    private var activePointerId = 0

    /**
     * 默认状态
     */
    private val defaultStatus: Int

    private var isWeekView = false

    /**
     * 星期栏
     */
    var weekBar: WeekBar? = null

    /**
     * 自定义ViewPager，月视图
     */
    lateinit var monthView: MonthViewPager

    /**
     * 日历
     */
    var calendarView: CalendarView? = null

    /**
     * 自定义的周视图
     */
    lateinit var weekPager: WeekViewPager

    /**
     * 年视图
     */
    lateinit var yearView: YearViewPager

    /**
     * ContentView
     */
    lateinit var contentView: ViewGroup

    /**
     * 手势模式
     */
    private val gestureMode: Int
    private var calendarShowMode: Int
    private var contentViewTranslateY = 0 //ContentView  可滑动的最大距离距离 , 固定 = 0
    private var viewPagerTranslateY = 0 // ViewPager可以平移的距离，不代表mMonthView的平移距离
    private var startY = 0f
    private var downY = 0f
    private var lastY = 0f
    private var lastX = 0f
    private val verticalY = 50f //竖直方向上滑动的临界值，大于这个值认为是竖直滑动
    private var isAnimating = false

    /**
     * 内容布局id
     */
    private val contentViewId: Int

    /**
     * 手速判断
     */
    private val velocityTracker: VelocityTracker
    private val maximumVelocity: Int
    private var itemHeight = 0
    private var viewDelegate = CalendarViewDelegate(context, attrs)

    override fun onFinishInflate() {
        super.onFinishInflate()
        monthView = findViewById(R.id.calendarView_vpMonth)
        weekPager = findViewById(R.id.calendarView_vpWeek)
        if (childCount > 0) {
            calendarView = getChildAt(0) as? CalendarView
        }
        contentView = findViewById(contentViewId)
        yearView = findViewById(R.id.calendarView_vpYear)
    }

    init {
        orientation = VERTICAL
        val array = context.obtainStyledAttributes(attrs, R.styleable.CalendarLayout)
        contentViewId = array.getResourceId(R.styleable.CalendarLayout_calendar_content_view_id, 0)
        defaultStatus = array.getInt(R.styleable.CalendarLayout_default_status, STATUS_EXPAND)
        calendarShowMode = array.getInt(
            R.styleable.CalendarLayout_calendar_show_mode,
            CALENDAR_SHOW_MODE_BOTH_MONTH_WEEK_VIEW
        )
        gestureMode = array.getInt(R.styleable.CalendarLayout_gesture_mode, GESTURE_MODE_DEFAULT)
        array.recycle()

        velocityTracker = VelocityTracker.obtain()
        val configuration = ViewConfiguration.get(context)
        configuration.scaledTouchSlop
        maximumVelocity = configuration.scaledMaximumFlingVelocity
    }

    /**
     * 初始化
     *
     * @param delegate delegate
     */
    fun setup(delegate: CalendarViewDelegate) {
        viewDelegate = delegate
        itemHeight = viewDelegate.calendarItemHeight
        initCalendarPosition(
            if (delegate.selectedCalendar.isAvailable)
                delegate.selectedCalendar else delegate.createCurrentDate()
        )
        updateContentViewTranslateY()
    }

    /**
     * 初始化当前时间的位置
     *
     * @param cur 当前日期时间
     */
    private fun initCalendarPosition(cur: Calendar) {
        val diff = CalendarUtil.getMonthViewStartDiff(cur, viewDelegate.weekStart)
        val size = diff + cur.day - 1
        updateSelectPosition(size)
    }

    /**
     * 当前第几项被选中，更新平移量
     *
     * @param selectPosition 月视图被点击的position
     */
    fun updateSelectPosition(selectPosition: Int) {
        val line = (selectPosition + 7) / 7
        viewPagerTranslateY = (line - 1) * itemHeight
    }

    /**
     * 设置选中的周，更新位置
     *
     * @param week week
     */
    fun updateSelectWeek(week: Int) {
        viewPagerTranslateY = (week - 1) * itemHeight
    }

    /**
     * 更新内容ContentView可平移的最大距离
     */
    fun updateContentViewTranslateY() {
        val calendar = viewDelegate.indexCalendar
        contentViewTranslateY =
            if (viewDelegate.monthViewShowMode == CalendarViewDelegate.MODE_ALL_MONTH) {
                5 * itemHeight
            } else {
                CalendarUtil.getMonthViewHeight(
                    calendar.year, calendar.month,
                    itemHeight, viewDelegate.weekStart
                ) - itemHeight
            }
        //已经显示周视图，则需要动态平移contentView的高度
        if (weekPager.visibility == View.VISIBLE) {
            contentView.translationY = -contentViewTranslateY.toFloat()
        }
    }

    /**
     * 更新日历项高度
     */
    fun updateCalendarItemHeight() {
        itemHeight = viewDelegate.calendarItemHeight
        val calendar = viewDelegate.indexCalendar
        updateSelectWeek(CalendarUtil.getWeekFromDayInMonth(calendar, viewDelegate.weekStart))
        contentViewTranslateY =
            if (viewDelegate.monthViewShowMode == CalendarViewDelegate.MODE_ALL_MONTH) {
                5 * itemHeight
            } else {
                CalendarUtil.getMonthViewHeight(
                    calendar.year, calendar.month,
                    itemHeight, viewDelegate.weekStart
                ) - itemHeight
            }
        translationViewPager()
        if (weekPager.visibility == View.VISIBLE) {
            contentView.translationY = -contentViewTranslateY.toFloat()
        }
    }

    /**
     * 隐藏日历
     */
    fun hideCalendarView() {
        if (calendarView == null) {
            return
        }
        calendarView?.visibility = View.GONE
        if (!isExpand) {
            expand(0)
        }
        requestLayout()
    }

    /**
     * 显示日历
     */
    fun showCalendarView() {
        calendarView?.visibility = View.VISIBLE
        requestLayout()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (gestureMode == GESTURE_MODE_DISABLED
            || calendarShowMode == CALENDAR_SHOW_MODE_ONLY_MONTH_VIEW
            || calendarShowMode == CALENDAR_SHOW_MODE_ONLY_WEEK_VIEW
        ) { //禁用手势，或者只显示某种视图
            return false
        }
        if (viewDelegate.isShowYearSelectedLayout) {
            return false
        }
        if (calendarView == null || calendarView?.visibility == View.GONE) {
            return false
        }

        velocityTracker.addMovement(event)
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val index = event.actionIndex
                activePointerId = event.getPointerId(index)
                downY = y
                lastY = y
                return true
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                val index = event.actionIndex
                activePointerId = event.getPointerId(index)
                if (activePointerId == 0) {
                    //核心代码：就是让下面的 dy = y- mLastY == 0，避免抖动
                    lastY = event.getY(activePointerId)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                getPointerIndex(event, activePointerId)
                if (activePointerId == INVALID_POINTER) {
                    //如果切换了手指，那把mLastY换到最新手指的y坐标即可，核心就是让下面的 dy== 0，避免抖动
                    lastY = y
                    activePointerId = ACTIVE_POINTER
                }
                val dy = y - lastY

                //向上滑动，并且contentView平移到最大距离，显示周视图
                if (dy < 0 && contentView.translationY == -contentViewTranslateY.toFloat()) {
                    lastY = y
                    event.action = MotionEvent.ACTION_DOWN
                    dispatchTouchEvent(event)
                    weekPager.visibility = View.VISIBLE
                    monthView.visibility = View.INVISIBLE
                    if (!isWeekView) {
                        viewDelegate.viewChangeListener?.onViewChange(false)
                    }
                    isWeekView = true
                    return true
                }
                hideWeek(false)

                //向下滑动，并且contentView已经完全平移到底部
                if (dy > 0 && contentView.translationY + dy >= 0) {
                    contentView.translationY = 0f
                    translationViewPager()
                    lastY = y
                    return super.onTouchEvent(event)
                }

                //向上滑动，并且contentView已经平移到最大距离，则contentView平移到最大的距离
                if (dy < 0 && contentView.translationY + dy <= -contentViewTranslateY) {
                    contentView.translationY = -contentViewTranslateY.toFloat()
                    translationViewPager()
                    lastY = y
                    return super.onTouchEvent(event)
                }
                //否则按比例平移
                contentView.translationY = contentView.translationY + dy
                translationViewPager()
                lastY = y
                return super.onTouchEvent(event)
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_POINTER_UP -> {
                val pointerIndex = getPointerIndex(event, activePointerId)
                if (activePointerId == INVALID_POINTER) {
                    return super.onTouchEvent(event)
                }
                lastY = event.getY(pointerIndex)
                return super.onTouchEvent(event)
            }
            MotionEvent.ACTION_UP -> {
                val velocityTracker = velocityTracker
                velocityTracker.computeCurrentVelocity(1000, maximumVelocity.toFloat())
                val mYVelocity = velocityTracker.yVelocity
                if (contentView.translationY == 0f
                    || contentView.translationY == contentViewTranslateY.toFloat()
                ) {
                    expand()
                    return super.onTouchEvent(event)
                }
                if (abs(mYVelocity) >= 800) {
                    if (mYVelocity < 0) {
                        shrink()
                    } else {
                        expand()
                    }
                    return super.onTouchEvent(event)
                }
                if (event.y - downY > 0) {
                    expand()
                } else {
                    shrink()
                }
                return super.onTouchEvent(event)
            }
        }
        return super.onTouchEvent(event)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (isAnimating) {
            return super.dispatchTouchEvent(ev)
        }
        if (gestureMode == GESTURE_MODE_DISABLED) {
            return super.dispatchTouchEvent(ev)
        }
        if (calendarView == null
            || calendarView?.visibility == View.GONE
            || contentView.visibility != View.VISIBLE
        ) {
            return super.dispatchTouchEvent(ev)
        }
        if (calendarShowMode == CALENDAR_SHOW_MODE_ONLY_MONTH_VIEW
            || calendarShowMode == CALENDAR_SHOW_MODE_ONLY_WEEK_VIEW
        ) {
            return super.dispatchTouchEvent(ev)
        }
        if (yearView.visibility == View.VISIBLE || viewDelegate.isShowYearSelectedLayout) {
            return super.dispatchTouchEvent(ev)
        }

        val y = ev.y
        when (ev.action) {
            MotionEvent.ACTION_MOVE -> {
                val dy = y - lastY

                /*
                 * 如果向下滚动，有 2 种情况处理 且y在ViewPager下方
                 * 1、RecyclerView 或者其它滚动的View，当mContentView滚动到顶部时，拦截事件
                 * 2、非滚动控件，直接拦截事件
                 */if (dy > 0 && contentView.translationY == -contentViewTranslateY.toFloat()) {
                    if (isScrollTop) {
                        requestDisallowInterceptTouchEvent(false) //父View向子View拦截分发事件
                        return super.dispatchTouchEvent(ev)
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (isAnimating) {
            return true
        }
        if (gestureMode == GESTURE_MODE_DISABLED) {
            return false
        }
        if (calendarView == null
            || calendarView?.visibility == View.GONE
            || contentView.visibility != View.VISIBLE
        ) {
            return super.onInterceptTouchEvent(ev)
        }
        if (calendarShowMode == CALENDAR_SHOW_MODE_ONLY_MONTH_VIEW
            || calendarShowMode == CALENDAR_SHOW_MODE_ONLY_WEEK_VIEW
        ) {
            return false
        }
        if (yearView.visibility == View.VISIBLE || viewDelegate.isShowYearSelectedLayout) {
            return super.onInterceptTouchEvent(ev)
        }

        val x = ev.x
        val y = ev.y
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                val index = ev.actionIndex
                activePointerId = ev.getPointerId(index)
                downY = y
                lastY = y
                lastX = x
            }
            MotionEvent.ACTION_MOVE -> {
                val dy = y - lastY
                val dx = x - lastX

                /*
                   如果向上滚动，且ViewPager已经收缩，不拦截事件
                 */if (dy < 0 && contentView.translationY == -contentViewTranslateY.toFloat()) {
                    return false
                }

                /*
                 * 如果向下滚动，有 2 种情况处理 且y在ViewPager下方
                 * 1、RecyclerView 或者其它滚动的View，当mContentView滚动到顶部时，拦截事件
                 * 2、非滚动控件，直接拦截事件
                 */if (dy > 0 && contentView.translationY == -contentViewTranslateY.toFloat()
                    && y >= viewDelegate.calendarItemHeight + viewDelegate.weekBarHeight
                ) {
                    if (!isScrollTop) {
                        return false
                    }
                }
                if (dy > 0 && contentView.translationY == 0f
                    && y >= CalendarUtil.dipToPx(context, 98f)
                ) {
                    return false
                }
                if (abs(dy) > abs(dx)) { //纵向滑动距离大于横向滑动距离,拦截滑动事件
                    if ((dy > 0 && contentView.translationY <= 0)
                        || (dy < 0 && contentView.translationY >= -contentViewTranslateY)
                    ) {
                        lastY = y
                        return true
                    }
                }
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    private fun getPointerIndex(ev: MotionEvent, id: Int): Int {
        val activePointerIndex = ev.findPointerIndex(id)
        if (activePointerIndex == -1) {
            activePointerId = INVALID_POINTER
        }
        return activePointerIndex
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var newHeightMeasureSpec = heightMeasureSpec
        if (calendarView == null) {
            super.onMeasure(widthMeasureSpec, newHeightMeasureSpec)
            return
        }
        val year = viewDelegate.indexCalendar.year
        val month = viewDelegate.indexCalendar.month
        val weekBarHeight = (CalendarUtil.dipToPx(context, 1f) + viewDelegate.weekBarHeight)
        val monthHeight = (CalendarUtil.getMonthViewHeight(
            year, month,
            viewDelegate.calendarItemHeight,
            viewDelegate.weekStart,
            viewDelegate.monthViewShowMode
        )
                + weekBarHeight)

        var height = MeasureSpec.getSize(newHeightMeasureSpec)
        if (viewDelegate.isFullScreenCalendar) {
            super.onMeasure(widthMeasureSpec, newHeightMeasureSpec)
            val heightSpec = MeasureSpec.makeMeasureSpec(
                height - weekBarHeight - viewDelegate.calendarItemHeight,
                MeasureSpec.EXACTLY
            )
            contentView.measure(widthMeasureSpec, heightSpec)
            contentView.layout(
                contentView.left,
                contentView.top,
                contentView.right,
                contentView.bottom
            )
            return
        }
        if (monthHeight >= height && monthView.height > 0) {
            height = monthHeight
            newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                monthHeight +
                        weekBarHeight +
                        viewDelegate.weekBarHeight, MeasureSpec.EXACTLY
            )
        } else if (monthHeight < height && monthView.height > 0) {
            newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        }
        val h = if (calendarShowMode == CALENDAR_SHOW_MODE_ONLY_MONTH_VIEW ||
            calendarView!!.visibility == View.GONE
        ) {
            height - if (calendarView!!.visibility == View.GONE) 0 else calendarView!!.height
        } else if (gestureMode == GESTURE_MODE_DISABLED && !isAnimating) {
            if (isExpand) {
                height - monthHeight
            } else {
                height - weekBarHeight - itemHeight
            }
        } else {
            height - weekBarHeight - itemHeight
        }
        super.onMeasure(widthMeasureSpec, newHeightMeasureSpec)
        val heightSpec = MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY)
        contentView.measure(widthMeasureSpec, heightSpec)
        contentView.layout(contentView.left, contentView.top, contentView.right, contentView.bottom)
    }

    /**
     * 平移ViewPager月视图
     */
    private fun translationViewPager() {
        val percent = contentView.translationY * 1.0f / contentViewTranslateY
        monthView.translationY = viewPagerTranslateY * percent
    }

    fun setModeBothMonthWeekView() {
        calendarShowMode = CALENDAR_SHOW_MODE_BOTH_MONTH_WEEK_VIEW
        requestLayout()
    }

    fun setModeOnlyWeekView() {
        calendarShowMode = CALENDAR_SHOW_MODE_ONLY_WEEK_VIEW
        requestLayout()
    }

    fun setModeOnlyMonthView() {
        calendarShowMode = CALENDAR_SHOW_MODE_ONLY_MONTH_VIEW
        requestLayout()
    }

    override fun onSaveInstanceState(): Parcelable? {
        return Bundle().apply {
            val parcelable = super.onSaveInstanceState()
            putParcelable("super", parcelable)
            putBoolean("isExpand", isExpand)
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val bundle = state as? Bundle
        bundle?.apply {
            val superData = getParcelable<Parcelable>("super")
            val isExpand = getBoolean("isExpand")
            if (isExpand) {
                post { expand(0) }
            } else {
                post { shrink(0) }
            }
            super.onRestoreInstanceState(superData)
        }
    }

    /**
     * 是否展开了
     *
     * @return isExpand
     */
    val isExpand: Boolean
        get() = monthView.visibility == View.VISIBLE

    /**
     * 展开
     *
     * @param duration 时长
     * @return 展开是否成功
     */
    fun expand(duration: Int = 240): Boolean {
        if (isAnimating || calendarShowMode == CALENDAR_SHOW_MODE_ONLY_WEEK_VIEW) {
            return false
        }
        if (monthView.visibility != View.VISIBLE) {
            weekPager.visibility = View.GONE
            onShowMonthView()
            isWeekView = false
            monthView.visibility = View.VISIBLE
        }
        val objectAnimator = ObjectAnimator.ofFloat(
            contentView,
            "translationY", contentView.translationY, 0f
        )
        objectAnimator.duration = duration.toLong()
        objectAnimator.addUpdateListener { animation ->
            val currentValue = animation.animatedValue as Float
            val percent = currentValue * 1.0f / contentViewTranslateY
            monthView.translationY = viewPagerTranslateY * percent
            isAnimating = true
        }
        objectAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                isAnimating = false
                if (gestureMode == GESTURE_MODE_DISABLED) {
                    requestLayout()
                }
                hideWeek(true)
                if (isWeekView) {
                    viewDelegate.viewChangeListener?.onViewChange(true)
                    isWeekView = false
                }
            }
        })
        objectAnimator.start()
        return true
    }

    /**
     * 收缩
     *
     * @param duration 时长
     * @return 成功或者失败
     */
    fun shrink(duration: Int = 240): Boolean {
        if (gestureMode == GESTURE_MODE_DISABLED) {
            requestLayout()
        }
        if (isAnimating) {
            return false
        }
        val objectAnimator = ObjectAnimator.ofFloat(
            contentView,
            "translationY", contentView.translationY, -contentViewTranslateY.toFloat()
        )
        objectAnimator.duration = duration.toLong()
        objectAnimator.addUpdateListener { animation ->
            val currentValue = animation.animatedValue as Float
            val percent = currentValue * 1.0f / contentViewTranslateY
            monthView.translationY = viewPagerTranslateY * percent
            isAnimating = true
        }
        objectAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                isAnimating = false
                showWeek()
                isWeekView = true
            }
        })
        objectAnimator.start()
        return true
    }

    /**
     * 初始化状态
     */
    fun initStatus() {
        if ((defaultStatus == STATUS_SHRINK ||
                    calendarShowMode == CALENDAR_SHOW_MODE_ONLY_WEEK_VIEW) &&
            calendarShowMode != CALENDAR_SHOW_MODE_ONLY_MONTH_VIEW
        ) {
            post {
                val objectAnimator = ObjectAnimator.ofFloat(
                    contentView,
                    "translationY", contentView.translationY, -contentViewTranslateY.toFloat()
                )
                objectAnimator.duration = 0
                objectAnimator.addUpdateListener { animation ->
                    val currentValue = animation.animatedValue as Float
                    val percent = currentValue * 1.0f / contentViewTranslateY
                    monthView.translationY = viewPagerTranslateY * percent
                    isAnimating = true
                }
                objectAnimator.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        isAnimating = false
                        isWeekView = true
                        showWeek()
                        viewDelegate.viewChangeListener?.onViewChange(false)
                    }
                })
                objectAnimator.start()
            }
        } else {
            post { viewDelegate.viewChangeListener?.onViewChange(true) }
        }
    }

    /**
     * 隐藏周视图
     */
    private fun hideWeek(isNotify: Boolean) {
        if (isNotify) {
            onShowMonthView()
        }
        weekPager.visibility = View.GONE
        monthView.visibility = View.VISIBLE
    }

    /**
     * 显示周视图
     */
    private fun showWeek() {
        onShowWeekView()
        if (weekPager.adapter != null) {
            weekPager.adapter?.notifyDataSetChanged()
            weekPager.visibility = View.VISIBLE
        }
        monthView.visibility = View.INVISIBLE
    }

    /**
     * 周视图显示事件
     */
    private fun onShowWeekView() {
        if (weekPager.visibility == View.VISIBLE) {
            return
        }
        if (!isWeekView) {
            viewDelegate.viewChangeListener?.onViewChange(false)
        }
    }

    /**
     * 周视图显示事件
     */
    private fun onShowMonthView() {
        if (monthView.visibility == View.VISIBLE) {
            return
        }
        if (isWeekView) {
            viewDelegate.viewChangeListener?.onViewChange(true)
        }
    }

    /**
     * ContentView是否滚动到顶部 如果完全不适合，就复写这个方法
     *
     * @return 是否滚动到顶部
     */
    protected val isScrollTop: Boolean
        get() {
            if (contentView is CalendarScrollView) {
                return (contentView as CalendarScrollView).isScrollToTop()
            }
            if (contentView is RecyclerView) {
                return (contentView as RecyclerView).computeVerticalScrollOffset() == 0
            }
            if (contentView is AbsListView) {
                var result = false
                val listView = contentView as AbsListView
                if (listView.firstVisiblePosition == 0) {
                    val topChildView = listView.getChildAt(0)
                    result = topChildView.top == 0
                }
                return result
            }
            return contentView.scrollY == 0
        }

    /**
     * 隐藏内容布局
     */
    @SuppressLint("NewApi")
    fun hideContentView() {
        contentView.animate()
            .translationY(height - monthView.height.toFloat())
            .setDuration(220)
            .setInterpolator(LinearInterpolator())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    contentView.visibility = View.INVISIBLE
                    contentView.clearAnimation()
                }
            })
    }

    /**
     * 显示内容布局
     */
    @SuppressLint("NewApi")
    fun showContentView() {
        contentView.translationY = height - monthView.height.toFloat()
        contentView.visibility = View.VISIBLE
        contentView.animate()
            .translationY(0f)
            .setDuration(180)
            .setInterpolator(LinearInterpolator())
            .setListener(object : AnimatorListenerAdapter() {
            })
    }

    private val calendarViewHeight: Int
        get() = if (monthView.visibility == View.VISIBLE)
            viewDelegate.weekBarHeight + monthView.height
        else
            viewDelegate.weekBarHeight + viewDelegate.calendarItemHeight

    /**
     * 如果有十分特别的ContentView，可以自定义实现这个接口
     */
    fun interface CalendarScrollView {

        /**
         * 是否滚动到顶部
         *
         * @return 是否滚动到顶部
         */
        fun isScrollToTop(): Boolean
    }

    companion object {
        private const val ACTIVE_POINTER = 1
        private const val INVALID_POINTER = -1

        /**
         * 周月视图
         */
        private const val CALENDAR_SHOW_MODE_BOTH_MONTH_WEEK_VIEW = 0

        /**
         * 仅周视图
         */
        private const val CALENDAR_SHOW_MODE_ONLY_WEEK_VIEW = 1

        /**
         * 仅月视图
         */
        private const val CALENDAR_SHOW_MODE_ONLY_MONTH_VIEW = 2

        /**
         * 默认展开
         */
        private const val STATUS_EXPAND = 0

        /**
         * 默认收缩
         */
        private const val STATUS_SHRINK = 1

        /**
         * 默认手势
         */
        private const val GESTURE_MODE_DEFAULT = 0

        /**
         * 禁用手势
         */
        private const val GESTURE_MODE_DISABLED = 2
    }
}