package io.video.ijkplayer.listener

import android.graphics.Bitmap
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.Surface
import android.view.View


/**
 * by JFZ
 * 2025/4/10
 * desc：
 **/

interface IJKMeasureListener {

    fun setVideoSize(width: Int, height: Int, sar_num: Int, sar_den: Int)

}

interface OnSurfaceListener {
    fun surfaceCreated(surface: Surface) {}

    fun surfaceChanged(surface: Surface, width: Int, height: Int) {
    }

    fun surfaceDestroyed(surface: Surface) {
    }

    fun surfaceUpdated(surface: Surface) {
    }
}

interface IJKGestureListener : GestureDetector.OnGestureListener,
    GestureDetector.OnDoubleTapListener {

    fun onTouchEvent(e: MotionEvent)

    fun onScaleBegin(detector: ScaleGestureDetector)

    fun onScale(detector: ScaleGestureDetector)

    fun onScaleEnd(detector: ScaleGestureDetector)

}

interface IJKShotListener {

    fun onShotBitmap(bitmap: Bitmap)
}

interface IJKRenderView {

    fun setIJKScaleType(scaleType: IJKScaleType)

    fun setVideoSize(width: Int, height: Int, sar_num: Int, sar_den: Int) {

    }

    fun getIJKScaleType(): IJKScaleType

    fun getVideoWidth(): Int

    fun getVideoHeight(): Int

    fun getVideoSarNum(): Int

    fun getVideoSarDen(): Int

    fun getRenderView(): View

    fun setOnSurfaceListener(listener: OnSurfaceListener)

    /**
     * 获取当前画面的bitmap，没有返回空
     * isHigh:是否高清
     */
    fun initCover(isHigh: Boolean): Bitmap

    fun taskShotPicture(isHigh: Boolean, listener: IJKShotListener)

}