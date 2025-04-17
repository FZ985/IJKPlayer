package io.video.ijkplayer.helper

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.customview.widget.ViewDragHelper
import io.video.ijkplayer.listener.IJKGestureListener
import io.video.ijkplayer.view.IJKPlayer
import kotlin.math.absoluteValue


/**
 * by JFZ
 * 2025/4/11
 * desc：
 **/
@SuppressLint("ClickableViewAccessibility")
class GestureDetectorHelper(
    context: Context,
    private val ijkView: IJKPlayer,
    private val targetView: View,
    gesView: View,
    private val gesListener: IJKGestureListener?
) {

    private var gestureDetector: GestureDetector? = null

    private val defaultFactor = 2f

    private var scaleFactor = defaultFactor

    private var isScaling = false

    private var currentDelta = 0f

    private var enableScale = true

    //滑动方向
    private val DIRECTION_NONE = 0
    private val DIRECTION_HORIZONTAL = 1
    private val DIRECTION_VERTICAL = 2
    private val DIRECTION_CUSTOM = 3
    private var gestureDirection = DIRECTION_NONE

    private var isScrollLeft = false
    private var isScrollTop = false
    private var isScrollRight = false
    private var isScrollBottom = false

    private val scaleGestureDetector =
        ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {

            override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                // 设置缩放中心
                targetView.pivotX = detector.focusX
                targetView.pivotY = detector.focusY
                gesListener?.onScaleBegin(detector)
                return true
            }

            override fun onScale(detector: ScaleGestureDetector): Boolean {
                if (enableScale) {
                    if (!ijkView.isPlaying()) {
                        gesListener?.onScale(detector)
                        return false
                    }
                    var factor = detector.scaleFactor
                    if (factor <= 1f) {
                        gesListener?.onScale(detector)
                        return false
                    } else {
                        isScaling = true
                        val delta = (factor - 1.0f).absoluteValue
                        if (delta < 0.02f) {
                            // 小于多少的缩放变化视为无效，滤除
                            gesListener?.onScale(detector)
                            return false
                        }

                        val newDelta = (currentDelta - delta).absoluteValue
                        if (newDelta < 0.02f) {
                            gesListener?.onScale(detector)
                            return false
                        } else {
                            gesListener?.onScale(detector)
                            currentDelta = delta
                            // 限制缩放速度：避免一次缩放太多
                            if (factor > 1.05f) factor = 1.05f

                            scaleFactor *= factor
                            scaleFactor = scaleFactor.coerceIn(1f, 3f)

                            targetView.scaleX = scaleFactor
                            targetView.scaleY = scaleFactor
                            return true
                        }
                    }
                }
                return false
            }

            override fun onScaleEnd(detector: ScaleGestureDetector) {
                gesListener?.onScaleEnd(detector)
            }
        })

    init {
        gestureDetector =
            GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
                override fun onDown(e: MotionEvent): Boolean {
                    return gesListener?.onDown(e) ?: super.onDown(e)
                }

                override fun onShowPress(e: MotionEvent) {
                    gesListener?.onShowPress(e)
                }

                override fun onSingleTapUp(e: MotionEvent): Boolean {
                    return gesListener?.onSingleTapUp(e) ?: super.onSingleTapUp(e)
                }

                override fun onScroll(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    distanceX: Float,
                    distanceY: Float
                ): Boolean {
                    return gesListener?.onScroll(e1, e2, distanceX, distanceY) ?: super.onScroll(
                        e1,
                        e2,
                        distanceX,
                        distanceY
                    )
                }

                override fun onLongPress(e: MotionEvent) {
                    gesListener?.onLongPress(e)
                }

                override fun onFling(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    velocityX: Float,
                    velocityY: Float
                ): Boolean {
                    return gesListener?.onFling(e1, e2, velocityX, velocityY) ?: super.onFling(
                        e1,
                        e2,
                        velocityX,
                        velocityY
                    )
                }

                override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                    return gesListener?.onSingleTapConfirmed(e) ?: super.onSingleTapConfirmed(e)
                }

                override fun onDoubleTap(e: MotionEvent): Boolean {
                    return gesListener?.onDoubleTap(e) ?: super.onDoubleTap(e)
                }

                override fun onDoubleTapEvent(e: MotionEvent): Boolean {
                    return gesListener?.onDoubleTapEvent(e) ?: super.onDoubleTapEvent(e)
                }

            })

        gesView.setOnTouchListener { _, event ->
            gesListener?.onTouchEvent(event)
            scaleGestureDetector.onTouchEvent(event)
            if (event.actionMasked == MotionEvent.ACTION_UP || event.actionMasked == MotionEvent.ACTION_CANCEL) {
                if (isScaling) {
                    isScaling = false
                    animateResetScale()
                }
            }
            when (event.actionMasked) {
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                    gestureDirection = DIRECTION_NONE
            }
            gestureDetector?.onTouchEvent(event) ?: false
        }
    }

    private fun animateResetScale() {
        ValueAnimator.ofFloat(scaleFactor, 1.0f).apply {
            duration = 200
            addUpdateListener {
                val value = it.animatedValue as Float
                targetView.scaleX = value
                targetView.scaleY = value
            }
            start()
        }
        scaleFactor = defaultFactor
    }

    fun setScaleEnable(enable: Boolean) {
        this.enableScale = enable
    }


    fun onScrollOperate(
        e1: MotionEvent?,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ) {
        if (e1 != null) {
            val deltaX = e2.x - e1.x
            val deltaY = e2.y - e1.y

            val absDeltaX = deltaX.absoluteValue
            val absDeltaY = deltaY.absoluteValue
            val threshold = 5f // 最小滑动距离，避免误触

            // 第一次滑动时锁定方向
            if (gestureDirection == DIRECTION_NONE) {
                gestureDirection = if (absDeltaX > threshold && absDeltaX > absDeltaY) {
                    ViewDragHelper.DIRECTION_HORIZONTAL
                } else if (absDeltaY > threshold && absDeltaY > absDeltaX) {
                    ViewDragHelper.DIRECTION_VERTICAL
                } else {
                    DIRECTION_CUSTOM
                }
            }

            // 已锁定方向后的处理
            if (gestureDirection == ViewDragHelper.DIRECTION_HORIZONTAL) {
                if (deltaX > 0) {
                    isScrollLeft = false
                    isScrollTop = false
                    isScrollBottom = false
                    //右
                    isScrollRight = true

                } else {
                    isScrollTop = false
                    isScrollBottom = false
                    isScrollRight = false
                    //左
                    isScrollLeft = true

                }
            } else if (gestureDirection == ViewDragHelper.DIRECTION_VERTICAL) {
                if (deltaY > 0) {
                    isScrollTop = false
                    isScrollLeft = false
                    isScrollRight = false
                    //下
                    isScrollBottom = true
                } else {
                    isScrollLeft = false
                    isScrollRight = false
                    isScrollBottom = false
                    //上
                    isScrollTop = true
                }
            } else {
                isScrollLeft = false
                isScrollRight = false
                isScrollBottom = false
                isScrollTop = false
            }
        } else {
            isScrollLeft = false
            isScrollRight = false
            isScrollBottom = false
            isScrollTop = false
        }
    }

    fun isScrollLeft() = this.isScrollLeft
    fun isScrollTop() = this.isScrollTop
    fun isScrollRight() = this.isScrollRight
    fun isScrollBottom() = this.isScrollBottom

}