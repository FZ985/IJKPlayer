package io.video.ijkplayer.controller

import android.view.MotionEvent
import android.view.ScaleGestureDetector
import io.video.ijkplayer.listener.OnSurfaceListener
import io.video.ijkplayer.utils.IjkUtils
import io.video.ijkplayer.view.IJKPlayer
import tv.danmaku.ijk.media.player.IMediaPlayer
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import tv.danmaku.ijk.media.player.IjkTimedText


/**
 * by JFZ
 * 2025/4/11
 * desc：
 **/
interface ControllerCase : OnSurfaceListener {

    fun onCreate(parentView: IJKPlayer, mp: IjkMediaPlayer)
    fun onAllCaseCreated(parentView: IJKPlayer, mp: IjkMediaPlayer) {}

    fun onStart() {}
    fun onResume() {}
    fun onPause() {}
    fun onStop() {}
    fun onDestroy() {}
    fun reset()

    fun onPrepared(mp: IMediaPlayer) {}
    fun onVideoStartBefore() {}
    fun onVideoStart() {}
    fun onVideoStop() {}
    fun onVideoPlay() {}
    fun onVideoPause() {}

    fun onCompletion(mp: IMediaPlayer) {}
    fun onBufferingUpdate(mp: IMediaPlayer, percent: Int) {}
    fun onSeekComplete(mp: IMediaPlayer) {}
    fun onVideoSizeChanged(width: Int, height: Int, sar_num: Int, sar_den: Int) {}
    fun onError(mp: IMediaPlayer, what: Int, extra: Int, e: Exception) {}
    fun onInfo(mp: IMediaPlayer, what: Int, extra: Int) {}
    fun onTimedText(mp: IMediaPlayer, text: IjkTimedText) {}


    fun onSingleTapUp(e: MotionEvent) {
    }

    fun onLongPress(e: MotionEvent) {}
    fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float,
        isLeft: Boolean,
        isRight: Boolean,
        isUp: Boolean,
        isDown: Boolean
    ) {
    }

    fun onFling(
        e1: MotionEvent?, e2: MotionEvent, velocityX: Float,
        velocityY: Float
    ) {
    }

    fun onShowPress(e: MotionEvent) {}
    fun onDown(e: MotionEvent) {}
    fun onDoubleTap(e: MotionEvent) {}
    fun onSingleTapConfirmed(e: MotionEvent) {}

    //view的实时touch事件
    fun onTouchEvent(e: MotionEvent) {}
    fun onScaleBegin(detector: ScaleGestureDetector) {}
    fun onScale(detector: ScaleGestureDetector) {}
    fun onScaleEnd(detector: ScaleGestureDetector) {}

    fun onFullScreen() {}
    fun onExitFullScreen() {}

    fun getId(): Int

    fun log(m: Any) {
        IjkUtils.log("ControllerCase", m.toString())
    }

}