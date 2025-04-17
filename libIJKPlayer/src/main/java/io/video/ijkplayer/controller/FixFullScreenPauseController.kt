package io.video.ijkplayer.controller

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import android.view.Gravity
import android.view.Surface
import android.view.View
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import io.video.ijkplayer.R
import io.video.ijkplayer.listener.IJKShotListener
import io.video.ijkplayer.view.IJKPlayer
import tv.danmaku.ijk.media.player.IMediaPlayer
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import kotlin.math.absoluteValue


/**
 * by JFZ
 * 2025/4/14
 * desc：处理 暂停时的ijk 全屏与退出全屏 的黑屏问题
 *
 * SurfaceView操作canvas绘制暂停bitmap无效，依旧黑屏；
 *
 *
 * 使用外层添加view遮罩处理
 **/
class FixFullScreenPauseController : ControllerCase {

    companion object {
        @JvmStatic
        val ID = "FixPauseController".hashCode().absoluteValue
    }

    private lateinit var ijkPlayer: IJKPlayer

    private var pauseBitmap: Bitmap? = null

    private val imageView: View by lazy {
        object : View(ijkPlayer.context) {
            override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
                try {
                    val scale = ijkPlayer.getIJKScaleType().javaClass.newInstance()
                    scale.prepareMeasure(
                        ijkPlayer.context,
                        widthMeasureSpec,
                        heightMeasureSpec,
                        ijkPlayer.getVideoWidth(),
                        ijkPlayer.getVideoHeight(),
                        ijkPlayer.getVideoSarNum(),
                        ijkPlayer.getVideoSarDen(),
                        0
                    )
                    setMeasuredDimension(
                        scale.getWidth(),
                        scale.getHeight()
                    )
                } catch (e: Exception) {
                    setMeasuredDimension(widthMeasureSpec, heightMeasureSpec)
                }
            }

            @SuppressLint("DrawAllocation")
            override fun onDraw(canvas: Canvas) {
                pauseBitmap?.let {
                    if (!it.isRecycled) {
                        val vWidth = ijkPlayer.getRenderView().width
                        val vHeight = ijkPlayer.getRenderView().height
                        val left = width / 2 - vWidth.toFloat() / 2
                        val rectF = RectF(left, 0f, vWidth.toFloat(), vHeight.toFloat())
                        canvas.drawBitmap(it, null, rectF, null)
                    }
                }
            }
        }
    }

    private val renderView: FrameLayout by lazy {
        ijkPlayer.findViewById(R.id.player_render_layer)
    }

    override fun onCreate(parentView: IJKPlayer, mp: IjkMediaPlayer) {
        this.ijkPlayer = parentView
    }

    override fun surfaceCreated(surface: Surface) {
        pauseBitmap?.let {
            if (!it.isRecycled && surface.isValid) {
                renderView.removeView(imageView)
                val params = FrameLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                )
                params.gravity = Gravity.CENTER
                renderView.addView(imageView, params)
            }
        }
    }

    override fun surfaceDestroyed(surface: Surface) {
    }

    override fun surfaceUpdated(surface: Surface) {
        releasePauseCover()
    }

    override fun onVideoPause() {
        if ((pauseBitmap == null || pauseBitmap!!.isRecycled)) {
            try {
                buildBitmap {
                    pauseBitmap = it
                }
            } catch (e: Exception) {
                e.printStackTrace()
                pauseBitmap = null
            }
        }
    }

    override fun onFullScreen() {
        if (ijkPlayer.isVideoPause()
            && (pauseBitmap == null || pauseBitmap!!.isRecycled)
        ) {
            try {
                buildBitmap {
                    pauseBitmap = it
                }
            } catch (e: Exception) {
                e.printStackTrace()
                pauseBitmap = null
            }
        } else {
            releasePauseCover()
        }
    }

    override fun onExitFullScreen() {
        if (ijkPlayer.isVideoPause()) {
            //全屏的位图还在，说明没播放，直接用原来的
            if (pauseBitmap != null
                && !pauseBitmap!!.isRecycled
            ) {
            } else {
                //不在了说明已经播放过，还是暂停的话，我们拿回来就好
                try {
                    buildBitmap {
                        pauseBitmap = it
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    pauseBitmap = null
                }
            }
        } else {
            releasePauseCover()
        }
    }

    private fun releasePauseCover() {
        try {
            pauseBitmap?.let {
                if (!ijkPlayer.isVideoPause() && !it.isRecycled) {
                    renderView.removeView(imageView)
                    it.recycle()
                    pauseBitmap = null
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onPrepared(mp: IMediaPlayer) {
        releasePauseCover()
    }

    override fun onVideoStart() {
        releasePauseCover()
    }

    override fun onVideoPlay() {
        releasePauseCover()
    }

    override fun reset() {
        releasePauseCover()
    }

    private fun buildBitmap(callback: (bitmap: Bitmap) -> Unit) {
        if (ijkPlayer.getRenderType() == IJKPlayer.TEXTURE) {
            callback(ijkPlayer.initCover(false))
        } else if (ijkPlayer.getRenderType() == IJKPlayer.SURFACE) {
            ijkPlayer.taskShotPicture(false, object : IJKShotListener {
                override fun onShotBitmap(bitmap: Bitmap) {
                    callback(bitmap)
                }
            })
        }
    }

    override fun getId() = FixFullScreenPauseController.ID
}