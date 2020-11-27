package com.skysoul.layoutmanagers.gallerscale

import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
import android.util.Log
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.*
import com.skysoul.layoutmanagers.toRectInt
import java.lang.Exception
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 *@author shenqichao
 *Created on 2020/10/13
 *@Description
 */
class GalleryScaleLayoutManager : RecyclerView.LayoutManager() {

    var maxShowCount = 17
        set(value) {
            field = if (value and 1 != 1) {
                value + 1
            } else {
                value
            }
            field = max(3,field)
        }

    /**
     *每一个靠后得缩放比例（主要是第一个，从第二个开始是用平方得算法）
     */
    var stepScale = 0.75f

    /**
     *遮挡比例
     */
    var maskRate = 1f / 3f
    var isCircle = true
    var isLooper = true
    val HORIZONTAL: Int = OrientationHelper.HORIZONTAL
    val VERTICAL: Int = OrientationHelper.VERTICAL
    var mOrientation = RecyclerView.HORIZONTAL


    private var mFirstVisiblePosition = 0
    private var mLastVisiblePos = 0
    private var mInitialSelectedPosition = 0

    var mCurSelectedPosition = -1

    var mCurSelectedView: View? = null


    private val mSnapHelper: LinearSnapHelper = LinearSnapHelper()

    private val mInnerScrollListener = InnerScrollListener()

    private val mCallbackInFling = false
    private var mRecyclerView: RecyclerView? = null

    /**
     *触发回弹的最左位置
     */
    private var maxLeft = 0

    /**
     *触发回弹的最右位置
     */
    private var maxRight = 0

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return if (mOrientation == VERTICAL) {
            RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        } else {
            RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }


    fun attach(recyclerView: RecyclerView) {
        this.attach(recyclerView, 0)
    }

    /**
     * @param recyclerView
     * @param selectedPosition
     */
    fun attach(recyclerView: RecyclerView, selectedPosition: Int) {
        mRecyclerView = recyclerView
        mInitialSelectedPosition = max(0, selectedPosition)
        recyclerView.layoutManager = this
        mSnapHelper.attachToRecyclerView(recyclerView)
        recyclerView.addOnScrollListener(mInnerScrollListener)
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        log("onLayoutChildren")
        if (itemCount == 0) {
            reset()
            detachAndScrapAttachedViews(recycler)
            return
        }
        if (state.isPreLayout) return
        if (state.itemCount != 0 && !state.didStructureChange()) {
            return
        }
        if (childCount == 0 || state.didStructureChange()) {
            reset()
        }
        mInitialSelectedPosition = min(max(0, mInitialSelectedPosition), itemCount - 1)
        detachAndScrapAttachedViews(recycler)
        firstFillCover(recycler, state)
    }


    private fun reset() {
        //when data set update keep the last selected position
        if (mCurSelectedPosition != -1) {
            mInitialSelectedPosition = mCurSelectedPosition
        }
        mInitialSelectedPosition = Math.min(Math.max(0, mInitialSelectedPosition), itemCount - 1)
        mFirstVisiblePosition = mInitialSelectedPosition
        mLastVisiblePos = mInitialSelectedPosition
        mCurSelectedPosition = -1
        if (mCurSelectedView != null) {
            mCurSelectedView!!.isSelected = false
            mCurSelectedView = null
        }
        mItemFrames.clear()
    }

    private fun firstFillCover(
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ) {
        if (mOrientation == HORIZONTAL) {
            firstFillWithHorizontal(recycler, state)
        } else {
//            firstFillWithVertical(recycler, state)
        }
        mInnerScrollListener.onScrolled(mRecyclerView!!, 0, 0)
    }


    private fun firstFillWithHorizontal(recycler: RecyclerView.Recycler, state: State) {
//        var leftEdge = getOrientationHelper().startAfterPadding
//        var rightEdge = getOrientationHelper().endAfterPadding
        //layout the init position view
        //layout the init position view

        maxLeft = paddingLeft
        maxRight = width - paddingRight

        val itemView = recycler.getViewForPosition(mInitialSelectedPosition)
        addView(itemView, 0)
        measureChildWithMargins(itemView, 0, 0)

        var rect = RectF().apply {
            val itemWidth = getDecoratedMeasuredWidth(itemView)
            val itemHeight = getDecoratedMeasuredHeight(itemView)
            left = (paddingLeft + (getHorizontalSpace() - itemWidth) / 2f).toInt().toFloat()
            top = (paddingTop + (getVerticalSpace() - itemHeight) / 2.0f).toInt().toFloat()
            right = left + itemWidth
            bottom = top + itemHeight
        }

        relayoutChildWithCenterItem(mInitialSelectedPosition, rect, true, recycler)
        //初始布局完毕后，根据布局计算回弹边界
        //非循环模式下，如果处在例如 49 居中得情况下，会导致右侧maxright 为49得rect right，这样是不准群得，所以取消重新计算回弹边距。友好点得话可以让用户自己设置：）
//        if (childCount > 1) {
//            var firstKeyFrame = mItemFrames[mFirstVisiblePosition]
//            if (!firstKeyFrame.kIamInReturn) {
//                maxLeft = firstKeyFrame.kRect.left
//            }
//            var lastFrame = mItemFrames[mLastVisiblePos]
//            if (!lastFrame.kIamInReturn) {
//                maxRight = lastFrame.kRect.right
//            }
//            var maxCenter = max(width / 2f - maxLeft, maxRight - width / 2f)
//            maxLeft = (width / 2f - maxCenter).toInt()
//            maxRight = (maxCenter + width / 2f).toInt()
//        }
//        maxShowCount = childCount
//      var   tmpMaxShowCount = max(mInitialSelectedPosition-mFirstVisiblePosition,mLastVisiblePos-mInitialSelectedPosition)*2+1
//        if(maxShowCount!=tmpMaxShowCount){
//            maxShowCount = tmpMaxShowCount
//            onLayoutChildren(recycler, state)
//        }
    }

    private fun layoutDecoratedWithRect(item: View, rect: Rect) {
        layoutDecorated(item, rect.left, rect.top, rect.right, rect.bottom)
    }

    private var mItemFrames: SparseArray<KeyFrame> = SparseArray()

    private fun isCanLooper() = isLooper && itemCount > childCount && childCount >1
//    private fun isCanLooper() = isLooper && itemCount > maxShowCount + 1

    private fun calculateWithCenterDisLeft(lastFrame: KeyFrame, itemView: View): KeyFrame {
        var itemWidth = getDecoratedMeasuredWidth(itemView)
        var itemHeight = getDecoratedMeasuredHeight(itemView)

        //先计算当前item应该layout在哪个区域
        var rect4Layout = RectF()
        var rect4LayoutNear = RectF()

        lastFrame.run {
            rect4Layout.set(
                kLayoutRect.left - itemWidth,
                kLayoutRect.top,
                kLayoutRect.left,
                kLayoutRect.bottom
            )
//            if (!lastFrame.kIamInReturn) {

            rect4LayoutNear.set(
                kRect.left.toFloat() - itemWidth,
                kRect.top.toFloat() - (itemHeight - kRect.height()) / 2,
                kRect.left.toFloat(),
                kRect.bottom.toFloat() + (itemHeight - kRect.height()) / 2
            )
//            } else {
//                rect4LayoutNear.set(
//                    kRect.left.toFloat() - itemWidth,
//                    kRect.top.toFloat() - (itemHeight - kRect.height()) / 2,
//                    kRect.left.toFloat(),
//                    kRect.bottom.toFloat() + (itemHeight - kRect.height()) / 2
//                )
//            }
        }

        return calculateWithCenterDis(itemView, rect4LayoutNear).apply {
            kLayoutRect = rect4Layout
        }

    }

    private fun calculateWithCenterDisRight(lastFrame: KeyFrame, itemView: View): KeyFrame {
        var itemWidth = getDecoratedMeasuredWidth(itemView)
        var itemHeight = getDecoratedMeasuredHeight(itemView)
        //先计算当前item应该layout在哪个区域
        var rect4Layout = RectF()
        var rect4LayoutNear = RectF()
        if (lastFrame.kPosition == 13 && lastFrame.kLayoutRect.right < 630) {
            itemWidth = itemWidth + 1
        }
        lastFrame.run {
            rect4Layout.set(
                kLayoutRect.right,
                kLayoutRect.top,
                kLayoutRect.right + itemWidth,
                kLayoutRect.bottom
            )
            rect4LayoutNear.set(
                kRect.right.toFloat(),
                kRect.top.toFloat() - (itemHeight - kRect.height()) / 2,
                kRect.right.toFloat() + itemWidth,
                kRect.bottom.toFloat() + (itemHeight - kRect.height()) / 2
            )
        }
        return calculateWithCenterDis(itemView, rect4LayoutNear).apply {
            kLayoutRect = rect4Layout
        }
    }

    //    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun calculateWithCenterDis(itemView: View, rect4Layout: RectF): KeyFrame {
        var itemWidth = getDecoratedMeasuredWidth(itemView)
        var dis = width / 2f - rect4Layout.centerX()

        var scale = stepScale.toDouble().pow((abs(dis) / itemWidth).toDouble())

        //借助matrix计算rect，先缩放后平移到遮挡1/3
        var matrix = Matrix()
        var centerX = rect4Layout.centerX()
        var centerY = rect4Layout.centerY()

        matrix.postTranslate((-centerX).toFloat(), (-centerY).toFloat())
        matrix.postScale(scale.toFloat(), scale.toFloat())
        matrix.postTranslate(centerX.toFloat(), centerY.toFloat())


        //计算如果要隐藏固定比例的话，应该位移多少
        var needTrans =
            itemWidth * scale * maskRate * min(
                1f,
                abs(dis) * 1f / itemWidth
            ) + itemWidth / 2f * (1 - scale)
        needTrans = if (dis < 0) {
            //在右边,需要左移
            -needTrans
        } else {
            needTrans
        }

        matrix.postTranslate(needTrans.toFloat(), 0f)
        var finalRect = RectF()
        matrix.mapRect(finalRect, rect4Layout)


        var needContinue = true
        if (dis > 0) {//在左边，判断左边界
            if (finalRect.left < maxLeft) {
                needContinue = false
                if (isCircle) {//如果是circle的需要回弹
                    var needReturnX = maxLeft - finalRect.left
                    matrix.postTranslate(needReturnX * 2f, 0f)
                    matrix.mapRect(finalRect, rect4Layout)
                }
            }
        } else {
            if (finalRect.right > maxRight) {
                needContinue = false
                if (isCircle) {//如果是circle的需要回弹
                    var needReturnX = finalRect.right - maxRight
                    matrix.postTranslate(-needReturnX * 2f, 0f)
                    matrix.mapRect(finalRect, rect4Layout)
                }
            }
        }

        var fr = finalRect.toRectInt()
        //借助matrix计算rect，先缩放后平移到遮挡1/3 end


        itemView.alpha = scale.toFloat()

        var result = FloatArray(9)
        matrix.getValues(result)

        itemView.scaleX = scale.toFloat()
        itemView.scaleY = scale.toFloat()
        itemView.translationX = (result[2] - rect4Layout.centerX() * (1 - scale)).toFloat()
        itemView.translationY = (result[5] - rect4Layout.centerY() * (1 - scale)).toFloat()
//        itemView.rotationY = 60f * (1 - scale.toFloat()) * (if (dis > 0) -1 else 1)


        layoutDecoratedWithRect(
            itemView, rect4Layout.toRectInt()
        )
        return KeyFrame().apply {
            kRect = fr
            kLayoutRect = rect4Layout
            kIamInReturn = !needContinue
        }

    }

    private fun isRectInCenter(rect4Layout: RectF): Boolean {

        return width / 2 >= rect4Layout.left && width / 2 <= rect4Layout.right
    }

    override fun scrollHorizontallyBy(
        dx: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State?
    ): Int {
        if (childCount == 0 || dx == 0) {
            return 0
        }
        var delta = 0
        //如果是循环的
        if (isCanLooper()) {
            delta = dx
        }

        log("scrollHorizontallyBy dx $dx")

        //findCenterItem 定义开始寻找的position

        var resultCenter = calculateCenterPFixRect(dx.toFloat(), recycler)
        var rect4Layout: RectF = resultCenter.rect
        var positionCenter = resultCenter.position
        var lastDx = resultCenter.delta

        relayoutChildWithCenterItem(positionCenter, rect4Layout, false, recycler)
        //调整左边视图层级
        var leftStartPosition = mFirstVisiblePosition
        while (leftStartPosition != positionCenter) {
            findViewByPosition(leftStartPosition)?.bringToFront()
            leftStartPosition = leftStartPosition.add(1)
        }
        //调整右边边视图层级
        var rightStartPosition = mLastVisiblePos
        while (rightStartPosition != positionCenter) {
            findViewByPosition(rightStartPosition)?.bringToFront()
            rightStartPosition = rightStartPosition.add(-1)
        }
        findViewByPosition(positionCenter)!!.bringToFront()
        return (dx - lastDx).toInt()
    }

    inner class CalculateCenterResult(var rect: RectF, var position: Int, var delta: Float) {
    }

    private fun calculateCenterPFixRect(dx: Float, recycler: Recycler): CalculateCenterResult {
        var positionCenter = -1
        if (mFirstVisiblePosition == 12) {
            positionCenter = -1
        }
        //如果itemtype不同，导致item的width差距很大，可能左边两个宽的，右边6个小的。这时中间位置的计算是有问题的；
        var positionCenterCur =
            if (mLastVisiblePos > mFirstVisiblePosition) (mLastVisiblePos + mFirstVisiblePosition) / 2
            else if (mLastVisiblePos < mFirstVisiblePosition) (mLastVisiblePos + itemCount + mFirstVisiblePosition) / 2 % itemCount
            else mFirstVisiblePosition
        //改用mCurSelectedPosition当作中间位
        positionCenterCur = mCurSelectedPosition
        log("positionCenterCur is $positionCenterCur")
        if (isCanLooper()) {
            var startFindP = positionCenterCur.add(-1)
            if (mItemFrames[startFindP] == null) {
                startFindP = startFindP.add(1)
            }
            var step = 1

            if (dx < 0) {
                step = -1
                startFindP = positionCenterCur.add(1)
                if (mItemFrames[startFindP] == null) {
                    startFindP = startFindP.add(-1)
                }
                if (startFindP == itemCount) {
                    startFindP = itemCount - 1
                }
            } else {
                if (!isCanLooper()) {
                    if (startFindP < 0) {
                        startFindP = 0
                    }
                }
            }
            var rect4Layout = RectF()
            try {
                mItemFrames[startFindP].kLayoutRect.also {
                    rect4Layout.apply {
                        left = it.left - dx
                        right = it.right - dx
                        top = it.top
                        bottom = it.bottom
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            //寻找中间位置的item
            while (positionCenter < 0) {
                if (isRectInCenter(rect4Layout)) {
                    positionCenter = startFindP
                    break
                } else {
                    startFindP = startFindP.add(step)
                    log("positionCenter find at $startFindP")

                    //这里width是0，所以会死循环 todo
                    var itemView = findViewByPosition(startFindP)
                    if (itemView == null) {
                        //需add之后，才能获取宽度，计算下一个rect
                        try {
                            itemView = recycler.getViewForPosition(startFindP)
                        }catch (e:Exception){
                            e.printStackTrace()
                            continue
                        }
                        addView(itemView)
                        measureChildWithMargins(itemView!!, 0, 0)
                    }
                    var itemWidth = getDecoratedMeasuredWidth(itemView)

                    rect4Layout.apply {
                        //当时计算错误，导致视图左右晃动（因为snap想让中间的item居中），因为算出下一个itemview的width之后直接让上一个rect的left和right都加了width（相当于将上一个rect平移了itemwidth，得到的并不是新item的rect）
                        //如果新的itemview的宽与上一个不一致的话，就会导致算出的新的itemview的rect不正确，布局也就不正确，然后snap会反向继续让其居中，就会出现来回晃动的死循环
//                    left += (step * itemWidth)
//                    right += (step * itemWidth)
                        if (step > 0) {
                            right += (step * itemWidth)
                            left = right - itemWidth

                        } else {
                            left += (step * itemWidth)
                            right = left + itemWidth
                        }
                    }
                }
            }
            return CalculateCenterResult(rect4Layout, positionCenter, 0f)
        } else {

            var startFindP = mCurSelectedPosition
            var rect4LayoutCur = mItemFrames[startFindP].kLayoutRect
            var step = if (dx > 0) 1 else -1
            var detal = dx
            while (positionCenter < 0) {

                var rect = RectF().apply {
                    left = rect4LayoutCur.left - detal
                    right = rect4LayoutCur.right - detal
                    top = rect4LayoutCur.top
                    bottom = rect4LayoutCur.bottom
                }
                if (dx > 0) {
                    if (rect.right > width / 2f) {
                        return CalculateCenterResult(rect, startFindP, 0f)
                    } else {
                        var dex = rect4LayoutCur.right - width / 2f
                        startFindP = startFindP.add(step)
                        if (startFindP == itemCount) {
                            startFindP = itemCount - 1

                            rect.apply {
                                right = width / 2f
                                left = right - rect4LayoutCur.width()
                            }

                            return CalculateCenterResult(
                                rect,
                                startFindP,
                                (dx - detal) - (rect.left - rect4LayoutCur.left)
                            )
                        }

                        var itemView = findViewByPosition(startFindP)
                        if (itemView == null) {
                            //需add之后，才能获取宽度，计算下一个rect
                            itemView = recycler.getViewForPosition(startFindP)
                            addView(itemView)
                            measureChildWithMargins(itemView, 0, 0)
                        }
                        var itemWidth = getDecoratedMeasuredWidth(itemView)

                        rect4LayoutCur.apply {
                            left = width / 2f
                            right = left + itemWidth
                        }
                        detal -= dex

                    }
                } else {
                    if (rect.left < width / 2f) {
                        return CalculateCenterResult(rect, startFindP, 0F)
                    } else {
                        var dex = width / 2f - rect4LayoutCur.left
                        startFindP = startFindP.add(step)
                        if (startFindP < 0) {
                            startFindP = 0

                            rect.apply {
                                left = width / 2f
                                right = left + rect4LayoutCur.width()
                            }

                            return CalculateCenterResult(
                                rect,
                                startFindP,
                                (dx - detal) - (rect.left - rect4LayoutCur.left)
                            )
                        }

                        var itemView = findViewByPosition(startFindP)
                        if (itemView == null) {
                            //需add之后，才能获取宽度，计算下一个rect
                            itemView = recycler.getViewForPosition(startFindP)
                            addView(itemView)
                            measureChildWithMargins(itemView, 0, 0)
                        }
                        var itemWidth = getDecoratedMeasuredWidth(itemView)

                        rect4LayoutCur.apply {
                            right = width / 2f
                            left = right - itemWidth
                        }
                        detal += dex

                    }
                }


            }


        }
        return CalculateCenterResult(RectF(), 0, 0f)
    }

    /**
     *从中间向两侧布，遇到回弹终止，并释放掉后面的
     */
    private fun relayoutChildWithCenterItem(
        positionCenter: Int,
        rect4Layout: RectF,
        isInit: Boolean,
        recycler: Recycler
    ) {
        mItemFrames.clear()//清空，重新布
        var itemView = findViewByPosition(positionCenter)!!
        itemView.bringToFront()
        //先计算中间位
        var curFrame = calculateWithCenterDis(itemView, rect4Layout)
        curFrame.kPosition = positionCenter
        mItemFrames.put(positionCenter, curFrame)

        reLayoutItemsByLastFrame(curFrame, 1, isInit, recycler)
        reLayoutItemsByLastFrame(curFrame, -1, isInit, recycler)
    }

    /**
     *从中间向两侧布，遇到回弹终止，并释放掉后面的
     */
    private fun reLayoutItemsByLastFrame(
        lastFrame: KeyFrame,
        step: Int,
        isInit: Boolean,
        recycler: Recycler
    ) {
        if (lastFrame.kIamInReturn) {
            //如果上一个已经回弹了，则
            recycleItemsFromPosition(lastFrame.kPosition, step, recycler)
            return
        }
//        if (isInit) {
//            var totalSideCount = (maxShowCount - 1) / 2
//            var positionOpt = if (step > 0) {
//                var p = mLastVisiblePos + 1
//                if (p < mInitialSelectedPosition) {
//                    p += itemCount
//                }
//                p
//            } else {
//                var p = mFirstVisiblePosition - 1
//                if (p > mInitialSelectedPosition) {
//                    p -= itemCount
//                }
//                p
//            }
//
//            if (positionOpt < 0 || positionOpt >= itemCount) {
//                if (!isCanLooper()) return
//            }
//            var curSideCount = abs(mInitialSelectedPosition - positionOpt)
//            if (curSideCount > totalSideCount) {
//                return
//            }
//
//        }
        var layoutPosition = lastFrame.kPosition.add(step)
        if (layoutPosition >= itemCount || layoutPosition < 0) return
        var childView = findViewByPosition(layoutPosition)
        if (childView == null) {
            childView = fillItem(recycler, layoutPosition)
        }

        var curFrame = if (step > 0) calculateWithCenterDisRight(
            lastFrame,
            childView!!
        ) else calculateWithCenterDisLeft(lastFrame, childView!!)
        curFrame.kPosition = layoutPosition

        //检测当前item布局后是否已经被完全覆盖，完全覆盖就recycle
        var curNeedRecycle = false
        if (curFrame.kIamInReturn) {
            if (isCircle) {
                if (step > 0) {
                    if (curFrame.kRect.right < lastFrame.kRect.right) {
                        curNeedRecycle = true
                    }
                } else {
                    if (curFrame.kRect.left > lastFrame.kRect.left) {
                        curNeedRecycle = true
                    }
                }
            }
        }
        if (curNeedRecycle) {
            removeAndRecycleView(childView, recycler)
            //如果上一个已经回弹了，则
            recycleItemsFromPosition(curFrame.kPosition, step, recycler)
            return
        }

        mItemFrames.put(layoutPosition, curFrame)
        if (step > 0) {
            mLastVisiblePos = layoutPosition
        } else {
            mFirstVisiblePosition = layoutPosition
        }
        reLayoutItemsByLastFrame(curFrame, step, isInit, recycler)

    }

    private fun fillItem(recycler: Recycler, position: Int): View {
        var itemView = recycler.getViewForPosition(position)
        addView(itemView, 0)
        measureChildWithMargins(itemView, 0, 0)
        return itemView
    }

    private fun Int.add(step: Int): Int {
        var result = this + step
        return if (!isCanLooper()) result else if (result >= itemCount) result - itemCount else if (result < 0) result + itemCount else result
    }

    /**
     *relayout时从中间向两边布，遇到已经回弹的位置时，将其后面的都回收掉
     */
    private fun recycleItemsFromPosition(lastPosition: Int, step: Int, recycler: Recycler) {
        var startPosition = lastPosition
        var stoped = false
        while (!stoped) {
            startPosition = startPosition.add(step)
            var found = false
            for (i in 0 until childCount) {
                var child = getChildAt(i)!!
                if (getPosition(child) == startPosition && mItemFrames[startPosition] == null) {
                    //如果找到position，并且此position对应的frame为空（relayout时还未进行layout）则recycle。防止recycle掉已经layout过的（数量较少时可能会触发）
                    log("removeAndRecycleView position ${startPosition}")
                    removeAndRecycleView(child, recycler)
                    found = true
                    break
                }
            }
            if (!found) {
                break
            }
        }

    }

    override fun removeAndRecycleView(child: View, recycler: Recycler) {

        super.removeAndRecycleView(child, recycler)
    }

    override fun canScrollHorizontally(): Boolean {
        return mOrientation == HORIZONTAL
    }

    private fun getHorizontalSpace(): Int {
        return width - paddingRight - paddingLeft
    }

    private fun getVerticalSpace(): Int {
        return height - paddingBottom - paddingTop
    }

    /**
     * Inner Listener to listen for changes to the selected item
     *
     * @author chensuilun
     */
    private inner class InnerScrollListener : RecyclerView.OnScrollListener() {
        var mState = 0
        var mCallbackOnIdle = false
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val snap = mSnapHelper.findSnapView(recyclerView.layoutManager)
            if (snap != null) {
                val selectedPosition = recyclerView.layoutManager!!.getPosition(snap)
                if (selectedPosition != mCurSelectedPosition) {
                    mCurSelectedView?.isSelected = false
                    mCurSelectedView = snap
                    mCurSelectedView?.isSelected = true
                    mCurSelectedPosition = selectedPosition
                    if (!mCallbackInFling && mState != SCROLL_STATE_IDLE) {
                        mCallbackOnIdle = true
                        return
                    }
                    mOnItemSelectedListener?.onItemSelected(
                        recyclerView,
                        snap,
                        mCurSelectedPosition
                    )
                }
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            mState = newState
            if (mState == SCROLL_STATE_IDLE) {
                val snap = mSnapHelper.findSnapView(recyclerView.layoutManager)
                if (snap != null) {
                    val selectedPosition = recyclerView.layoutManager!!.getPosition(snap)
                    if (selectedPosition != mCurSelectedPosition) {
                        mCurSelectedView?.isSelected = false
                        mCurSelectedView = snap
                        mCurSelectedView?.isSelected = true
                        mCurSelectedPosition = selectedPosition
                        mOnItemSelectedListener?.onItemSelected(
                            recyclerView,
                            snap,
                            mCurSelectedPosition
                        )
                    } else if (!mCallbackInFling && mOnItemSelectedListener != null && mCallbackOnIdle) {
                        mCallbackOnIdle = false
                        mOnItemSelectedListener.onItemSelected(
                            recyclerView,
                            snap,
                            mCurSelectedPosition
                        )
                    }
                } else {
                }
            }
        }
    }


    private val mOnItemSelectedListener: OnItemSelectedListener? = null

    override fun smoothScrollToPosition(
        recyclerView: RecyclerView?,
        state: RecyclerView.State?,
        position: Int
    ) {
        val linearSmoothScroller: GalleryScaleSmoothScroller =
            GalleryScaleSmoothScroller(
                recyclerView!!.context
            ) { p ->
                mItemFrames[p].kLayoutRect.centerX() - width / 2f
            }
        linearSmoothScroller.targetPosition = position
        startSmoothScroll(linearSmoothScroller)
    }

    override fun addView(child: View?, index: Int) {
        super.addView(child, index)
        log("addView total is $childCount")
    }

    private fun log(msg: String) {
        Log.d("sqc", msg)
    }

    override fun addView(child: View?) {
        super.addView(child)
        log("addView total is $childCount")
    }

}