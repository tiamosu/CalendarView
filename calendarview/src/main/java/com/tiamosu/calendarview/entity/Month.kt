package com.tiamosu.calendarview.entity

import androidx.annotation.Keep
import java.io.Serializable

/**
 * @author tiamosu
 * @date 2020/5/25.
 */
@Keep
class Month : Serializable {
    var diff = 0    //日期偏移
    var count = 0
    var month = 0
    var year = 0
}