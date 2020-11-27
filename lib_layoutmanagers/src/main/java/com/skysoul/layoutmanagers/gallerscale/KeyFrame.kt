package com.skysoul.layoutmanagers.gallerscale

import android.graphics.Rect
import android.graphics.RectF

internal class KeyFrame {
    //当前实际位置，用于计算相邻的layoutrect
    var kRect: Rect = Rect()

    //是否处于回弹
    var kIamInReturn = false

    //之际layout的rect
    var kLayoutRect = RectF()
    var kPosition = -1
}