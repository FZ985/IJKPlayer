package io.video.ijkplayer.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.view.PixelCopy
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import io.video.ijkplayer.listener.IJKMeasureListener
import io.video.ijkplayer.listener.IJKRenderView
import io.video.ijkplayer.listener.IJKShotListener
import io.video.ijkplayer.listener.OnSurfaceListener
import io.video.ijkplayer.scaletype.IJKScaleType
import io.video.ijkplayer.scaletype.NoneType
import io.video.ijkplayer.utils.IjkUtils


/**
 * by JFZ
 * 2025/4/10
 * desc：
 **/
@SuppressLint("ViewConstructor")
class IJKSurfaceView(context: Context, surfaceListener: OnSurfaceListener) : SurfaceView(context),
    IJKMeasureListener, IJKRenderView, SurfaceHolder.Callback {

    private var videoWidth = 0

    private var videoHeight = 0

    private var sarNum = 0

    private var sarDen = 0

    private var scaleType: IJKScaleType = NoneType()

    private val listeners = mutableListOf<OnSurfaceListener>()

    init {
        listeners.clear()
        setOnSurfaceListener(surfaceListener)
        holder.removeCallback(this)
        holder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        listeners.forEach { it.surfaceCreated(holder.surface) }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        listeners.forEach { it.surfaceChanged(holder.surface, width, height) }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        listeners.forEach { it.surfaceDestroyed(holder.surface) }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        this.scaleType.prepareMeasure(
            context,
            widthMeasureSpec,
            heightMeasureSpec,
            videoWidth,
            videoHeight,
            sarNum,
            sarDen,
            0
        )
        setMeasuredDimension(scaleType.getWidth(), scaleType.getHeight())
    }

    override fun setIJKScaleType(scaleType: IJKScaleType) {
        this.scaleType = scaleType
        requestLayout()
    }

    override fun getIJKScaleType(): IJKScaleType {
        return this.scaleType
    }

    override fun getVideoWidth(): Int {
        return videoWidth
    }

    override fun getVideoHeight(): Int {
        return videoHeight
    }

    override fun getVideoSarNum(): Int {
        return sarNum
    }

    override fun getVideoSarDen(): Int {
        return sarDen
    }

    override fun getRenderView(): View {
        return this
    }

    override fun setVideoSize(width: Int, height: Int, sar_num: Int, sar_den: Int) {
        videoWidth = width
        videoHeight = height
        this.sarNum = sar_num
        this.sarDen = sar_den
        requestLayout()
    }

    override fun setOnSurfaceListener(listener: OnSurfaceListener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
        }
    }

    override fun initCover(isHigh: Boolean): Bitmap {
        return if (isHigh) {
            Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        } else {
            Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        }
    }

    override fun taskShotPicture(isHigh: Boolean, listener: IJKShotListener) {
        val bitmap = initCover(isHigh)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val handlerThread = HandlerThread("PixelCopier")
                handlerThread.start()
                PixelCopy.request(
                    this, bitmap,
                    {
                        if (it == PixelCopy.SUCCESS) {
                            listener.onShotBitmap(bitmap)
                        }
                        handlerThread.quitSafely()
                    }, Handler()
                )
            } else {
                IjkUtils.log(
                    javaClass.simpleName,
                    "版本 Build.VERSION.SDK_INT < Build.VERSION_CODES.N not support taskShotPicture now"
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}

