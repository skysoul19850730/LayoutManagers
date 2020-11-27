package com.skysoul.layoutmanagers.gallerscale

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import java.lang.Math.abs

internal class GalleryScaleSmoothScroller(context: Context, private val dxWithPosition: (position:Int) -> Float) :
    LinearSmoothScroller(context) {
    override fun onTargetFound(targetView: View, state: RecyclerView.State, action: RecyclerView.SmoothScroller.Action) {

        var position = getChildPosition(targetView)

        var dx = dxWithPosition(position)

        val time = calculateTimeForDeceleration(abs(dx.toInt()))
        if (time > 0) {
            action.update((dx).toInt(), 0, time, mDecelerateInterpolator)
        }
    }
}