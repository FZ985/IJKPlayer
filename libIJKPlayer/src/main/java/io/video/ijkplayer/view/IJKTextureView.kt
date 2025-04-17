package io.video.ijkplayer.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.view.Surface
import android.view.TextureView
import android.view.View
import io.video.ijkplayer.listener.IJKMeasureListener
import io.video.ijkplayer.listener.IJKRenderView
import io.video.ijkplayer.listener.IJKShotListener
import io.video.ijkplayer.listener.OnSurfaceListener
import io.video.ijkplayer.scaletype.IJKScaleType
import io.video.ijkplayer.scaletype.NoneType


/**
 * by JFZ
 * 2025/4/10
 * desc：
 **/
@SuppressLint("ViewConstructor")
class IJKTextureView(context: Context, surfaceListener: OnSurfaceListener) : TextureView(context),
    IJKMeasureListener, IJKRenderView, TextureView.SurfaceTextureListener {

    private var videoWidth = 0

    private var videoHeight = 0

    private var sarNum = 0

    private var sarDen = 0

    private var scaleType: IJKScaleType = NoneType()

    private val listeners = mutableListOf<OnSurfaceListener>()

    private var mSurface: Surface? = null

    init {
        listeners.clear()
        setOnSurfaceListener(surfaceListener)
        surfaceTextureListener = this
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        mSurface = Surface(surface)
        mSurface?.let { sur ->
            listeners.forEach { it.surfaceCreated(sur) }
        }
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        mSurface?.let { sur ->
            listeners.forEach { it.surfaceChanged(sur, width, height) }
        }
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        mSurface?.let { sur ->
            listeners.forEach { it.surfaceDestroyed(sur) }
        }
        return true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
        mSurface?.let { sur ->
            listeners.forEach { it.surfaceUpdated(sur) }
        }
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
            getBitmap(
                Bitmap.createBitmap(
                    width, height, Bitmap.Config.ARGB_8888
                )
            )
        } else {
            getBitmap(
                Bitmap.createBitmap(
                    width, height, Bitmap.Config.RGB_565
                )
            )
        }
    }

    override fun taskShotPicture(isHigh: Boolean, listener: IJKShotListener) {
        listener.onShotBitmap(initCover(isHigh))
    }
}

