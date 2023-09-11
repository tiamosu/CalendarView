package com.tiamosu.calendarview.entity

import androidx.annotation.Keep
import com.tiamosu.calendarview.utils.CalendarUtil
import java.io.Serializable
import java.util.*

/**
 * 日历对象
 *
 * @author tiamosu
 * @date 2020/5/25.
 */
@Keep
class Calendar : Serializable, Comparable<Calendar?> {

    /**
     * 年
     */
    var year = 0

    /**
     * 月1-12
     */
    var month = 0

    /**
     * 如果是闰月，则返回闰月
     */
    var leapMonth = 0

    /**
     * 日1-31
     */
    var day = 0

    /**
     * 是否是闰年
     */
    var isLeapYear = false

    /**
     * 是否是本月,这里对应的是月视图的本月，而非当前月份，请注意
     */
    var isCurrentMonth = false

    /**
     * 是否是今天
     */
    var isCurrentDay = false

    /**
     * 农历字符串，没有特别大的意义，用来做简单的农历或者节日标记
     * 建议通过 lunarCalendar 获取完整的农历日期
     */
    var lunar: String = ""

    /**
     * 24节气
     */
    var solarTerm: String = ""

    /**
     * 公历节日
     */
    var gregorianFestival: String = ""

    /**
     * 传统农历节日
     */
    var traditionFestival: String = ""

    /**
     * 计划，可以用来标记当天是否有任务,这里是默认的，如果使用多标记，请使用下面API
     * using addScheme(int schemeColor,String scheme); multi scheme
     */
    var scheme: String = ""

    /**
     * 各种自定义标记颜色、没有则选择默认颜色，如果使用多标记，请使用下面API
     * using addScheme(int schemeColor,String scheme); multi scheme
     */
    var schemeColor = 0

    /**
     * 多标记
     * multi scheme,using addScheme();
     */
    var schemes: MutableList<Scheme>? = null

    /**
     * 是否是周末
     */
    var isWeekend = false

    /**
     * 星期,0-6 对应周日到周一
     */
    var week = 0

    /**
     * 获取完整的农历日期
     */
    var lunarCalendar: Calendar? = null

    fun addScheme(scheme: Scheme) {
        if (schemes == null) {
            schemes = ArrayList()
        }
        schemes?.add(scheme)
    }

    fun addScheme(schemeColor: Int, scheme: String?) {
        if (schemes == null) {
            schemes = ArrayList()
        }
        schemes?.add(Scheme(schemeColor, scheme))
    }

    fun addScheme(type: Int, schemeColor: Int, scheme: String?) {
        if (schemes == null) {
            schemes = ArrayList()
        }
        schemes?.add(Scheme(type, schemeColor, scheme))
    }

    fun addScheme(type: Int, schemeColor: Int, scheme: String?, other: String?) {
        if (schemes == null) {
            schemes = ArrayList()
        }
        schemes?.add(Scheme(type, schemeColor, scheme, other))
    }

    fun addScheme(schemeColor: Int, scheme: String?, other: String?) {
        if (schemes == null) {
            schemes = ArrayList()
        }
        schemes?.add(Scheme(schemeColor, scheme, other))
    }

    fun hasScheme(): Boolean {
        if ((schemes?.size ?: 0) != 0) {
            return true
        }
        return scheme.isNotBlank()
    }

    /**
     * 是否是相同月份
     *
     * @param calendar 日期
     * @return 是否是相同月份
     */
    fun isSameMonth(calendar: Calendar): Boolean {
        return year == calendar.year && month == calendar.month
    }

    /**
     * 比较日期
     *
     * @param other 日期
     * @return <0 0 >0
     */
    override fun compareTo(other: Calendar?): Int {
        return if (other == null) {
            1
        } else toString().compareTo(other.toString())
    }

    /**
     * 运算差距多少天
     *
     * @param calendar calendar
     * @return 运算差距多少天
     */
    fun differ(calendar: Calendar?): Int {
        return CalendarUtil.differ(this, calendar)
    }

    /**
     * 日期是否可用
     *
     * @return 日期是否可用
     */
    val isAvailable: Boolean
        get() = (year > 0) and (month > 0) and (day > 0) and (day <= 31) and (month <= 12) and (year >= 1900) and (year <= 2099)

    /**
     * 获取当前日历对应时间戳
     */
    val timeInMillis: Long
        get() {
            val calendar = java.util.Calendar.getInstance()
            calendar[java.util.Calendar.YEAR] = year
            calendar[java.util.Calendar.MONTH] = month - 1
            calendar[java.util.Calendar.DAY_OF_MONTH] = day
            return calendar.timeInMillis
        }

    override fun equals(other: Any?): Boolean {
        if (other is Calendar) {
            if (other.year == year && other.month == month && other.day == day) {
                return true
            }
        }
        return super.equals(other)
    }

    override fun toString(): String {
        return year.toString() + "" + (if (month < 10) "0$month" else month) + "" + if (day < 10) "0$day" else day
    }

    fun mergeScheme(calendar: Calendar?, defaultScheme: String) {
        if (calendar == null) {
            return
        }
        scheme = if (calendar.scheme.isBlank()) defaultScheme else calendar.scheme
        schemeColor = calendar.schemeColor
        schemes = calendar.schemes
    }

    fun clearScheme() {
        scheme = ""
        schemeColor = 0
        schemes = null
    }

    override fun hashCode(): Int {
        var result = year
        result = 31 * result + month
        result = 31 * result + leapMonth
        result = 31 * result + day
        result = 31 * result + isLeapYear.hashCode()
        result = 31 * result + isCurrentMonth.hashCode()
        result = 31 * result + isCurrentDay.hashCode()
        result = 31 * result + lunar.hashCode()
        result = 31 * result + solarTerm.hashCode()
        result = 31 * result + gregorianFestival.hashCode()
        result = 31 * result + traditionFestival.hashCode()
        result = 31 * result + scheme.hashCode()
        result = 31 * result + schemeColor
        result = 31 * result + (schemes?.hashCode() ?: 0)
        result = 31 * result + isWeekend.hashCode()
        result = 31 * result + week
        result = 31 * result + (lunarCalendar?.hashCode() ?: 0)
        return result
    }

    /**
     * 事件标记服务，现在多类型的事务标记建议使用这个
     */
    class Scheme : Serializable {
        var type = 0
        var shcemeColor = 0
        var scheme: String? = null
        var other: String? = null

        constructor(type: Int, shcemeColor: Int, scheme: String?, other: String?) {
            this.type = type
            this.shcemeColor = shcemeColor
            this.scheme = scheme
            this.other = other
        }

        constructor(type: Int, shcemeColor: Int, scheme: String?) {
            this.type = type
            this.shcemeColor = shcemeColor
            this.scheme = scheme
        }

        constructor(shcemeColor: Int, scheme: String?) {
            this.shcemeColor = shcemeColor
            this.scheme = scheme
        }

        constructor(shcemeColor: Int, scheme: String?, other: String?) {
            this.shcemeColor = shcemeColor
            this.scheme = scheme
            this.other = other
        }

    }

    companion object {
        private const val serialVersionUID = 141315161718191143L
    }
}