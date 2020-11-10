package com.tiamosu.calendarview.utils

import android.content.Context
import com.tiamosu.calendarview.R

/**
 * 干支纪年算法
 *
 * @author tiamosu
 * @date 2020/5/25.
 */
object TrunkBranchAnnals {

    /**
     * 天干字符串
     */
    private var trunkStrList: Array<String>? = null

    /**
     * 地支字符串
     */
    private var branchStrList: Array<String>? = null

    /**
     * 单独使用请先调用这个方法
     *
     * @param context context
     */
    fun init(context: Context) {
        if (trunkStrList?.isNotEmpty() == true) {
            return
        }
        trunkStrList = context.resources.getStringArray(R.array.trunk_string_array)
        branchStrList = context.resources.getStringArray(R.array.branch_string_array)
    }

    /**
     * 获取某一年对应天干文字
     *
     * @param year 年份
     * @return 天干由甲到癸，每10一轮回
     */
    fun getTrunkString(year: Int): String {
        return trunkStrList?.get(getTrunkInt(year)) ?: ""
    }

    /**
     * 获取某一年对应天干，
     *
     * @param year 年份
     * @return 4 5 6 7 8 9 10 1 2 3
     */
    fun getTrunkInt(year: Int): Int {
        val trunk = year % 10
        return if (trunk == 0) 9 else trunk - 1
    }

    /**
     * 获取某一年对应地支文字
     *
     * @param year 年份
     * @return 地支由子到亥，每12一轮回
     */
    fun getBranchString(year: Int): String {
        return branchStrList?.get(getBranchInt(year)) ?: ""
    }

    /**
     * 获取某一年对应地支
     *
     * @param year 年份
     * @return 4 5 6 7 8 9 10 11 12 1 2 3
     */
    fun getBranchInt(year: Int): Int {
        val branch = year % 12
        return if (branch == 0) 11 else branch - 1
    }

    /**
     * 获取干支纪年
     *
     * @param year 年份
     * @return 干支纪年
     */
    fun getTrunkBranchYear(year: Int): String {
        return String.format("%s%s", getTrunkString(year), getBranchString(year))
    }
}