package com.skysoul.layoutmanagers

import android.graphics.Rect
import android.graphics.RectF

/**
 *@author shenqichao
 *Created on 2020/10/29
 *@Description
 */
internal fun RectF.toRectInt(): Rect {
    return Rect().apply {
        top = this@toRectInt.top.toInt()
        bottom = this@toRectInt.bottom.toInt()
        left = this@toRectInt.left.toInt()
        right = this@toRectInt.right.toInt()
    }
}