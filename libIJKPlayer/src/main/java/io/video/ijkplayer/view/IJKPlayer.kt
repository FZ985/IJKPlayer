package io.video.ijkplayer.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import io.video.ijkplayer.IJKModel
import io.video.ijkplayer.controller.ControllerCase
import io.video.ijkplayer.databinding.IjkPlayerViewBinding
import io.video.ijkplayer.helper.GestureDetectorHelper
import io.video.ijkplayer.listener.IJKGestureListener
import io.video.ijkplayer.listener.IJKRenderView
import io.video.ijkplayer.listener.IJKShotListener
import io.video.ijkplayer.listener.OnSurfaceListener
import io.video.ijkplayer.scaletype.IJKScaleType
import io.video.ijkplayer.utils.IJKState
import io.video.ijkplayer.utils.IjkUtils
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import java.lang.reflect.InvocationTargetException
import java.util.LinkedList


/**
 * by JFZ
 * 2025/4/10
 * desc：ijk播放器
 **/
open class IJKPlayer : ConstraintLayout, IJKRenderView {

    companion object {

        /**
         * TextureView,默认
         */
        @JvmStatic
        val TEXTURE = 0

        /**
         * SurfaceView，与动画全屏的效果不是很兼容
         */
        @JvmStatic
        val SURFACE = 1

    }

    private var binding: IjkPlayerViewBinding

    private val mediaPlayer = IjkMediaPlayer()

    private val mHandler = Handler(Looper.getMainLooper())

    private val controllerList = mutableListOf<ControllerCase>()

    private var dataSource = IJKModel()

    private var isComplete = false
    private var isPause = false
    private var isStop = false
    private var isPrepared = false
    private var isFullScreen = false
    private var isError = false


    private var gestureHelper: GestureDetectorHelper? = null

    private var blockIndex = 0
    private var blockWidth = 0
    private var blockHeight = 0
    private var blockLayoutParams: ViewGroup.LayoutParams? = null
    private val CONTAINER_LIST = LinkedList<ViewGroup>()

    //保存系统状态ui
    private var mSystemUiVisibility = 0

    //是否隐藏虚拟按键
    private var enableNavKey = true

    //是否隐藏状态栏
    private var enableStatusBar = true

    //是否是竖屏全屏
    private var isPortraitFullscreen = false

    //渲染组件类型
    private var renderType = TEXTURE
    private lateinit var renderView: IJKRenderView
    private lateinit var mSurface: Surface
    private val surfaceListener = object : OnSurfaceListener {
        override fun surfaceCreated(surface: Surface) {
            mSurface = surface
            mediaPlayer.setSurface(surface)
            controllerList.forEach { it.surfaceCreated(surface) }
        }

        override fun surfaceChanged(surface: Surface, width: Int, height: Int) {
            controllerList.forEach { it.surfaceChanged(surface, width, height) }
        }

        override fun surfaceDestroyed(surface: Surface) {
            mediaPlayer.setSurface(null)
            controllerList.forEach { it.surfaceDestroyed(surface) }
        }

        override fun surfaceUpdated(surface: Surface) {
            controllerList.forEach { it.surfaceUpdated(surface) }
        }
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attr: AttributeSet?) : this(context, attr, 0)
    constructor(context: Context, attr: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attr,
        defStyleAttr
    ) {
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
        mediaPlayer.setOnNativeInvokeListener { _, _ -> true }

        binding = IjkPlayerViewBinding.inflate(LayoutInflater.from(context), this, true)
        setRenderType(renderType)
        isFullScreen =
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {// 竖屏
                false
            } else {// 横屏
                resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
            }
        controllerList.forEach { it.reset() }
        controllerList.clear()

        val controllerCase = this.getControllerCase()
        if (controllerCase.isNotEmpty()) {
            controllerList.addAll(controllerCase)
        }
        controllerList.forEach {
            it.onCreate(this, mediaPlayer)
        }
        controllerList.forEach {
            it.onAllCaseCreated(this, mediaPlayer)
        }
        this.onInit()
        initListener()
    }

    fun bindLifecycle(owner: LifecycleOwner) {
        owner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                controllerList.forEach { it.onStart() }
            }

            override fun onResume(owner: LifecycleOwner) {
                controllerList.forEach {
                    it.onResume()
                }
            }

            override fun onPause(owner: LifecycleOwner) {
                controllerList.forEach { it.onPause() }
            }

            override fun onStop(owner: LifecycleOwner) {
                controllerList.forEach { it.onStop() }
            }

            override fun onDestroy(owner: LifecycleOwner) {
                controllerList.forEach { it.onDestroy() }
                release()
            }
        })
    }

    open fun onInit() {}

    @SuppressLint("ClickableViewAccessibility")
    private fun initListener() {
        mediaPlayer.setOnPreparedListener { mp ->
            isPrepared = true
            isComplete = false
            isPause = false
            isStop = false
            controllerList.forEach { it.onPrepared(mp) }
        }

        mediaPlayer.setOnInfoListener { mp, what, extra ->
            controllerList.forEach { it.onInfo(mp, what, extra) }
            true
        }

        mediaPlayer.setOnBufferingUpdateListener { mp, percent ->
            controllerList.forEach { it.onBufferingUpdate(mp, percent) }
        }

        mediaPlayer.setOnTimedTextListener { mp, text ->
            controllerList.forEach { it.onTimedText(mp, text) }
        }

        mediaPlayer.setOnSeekCompleteListener { mp ->
            controllerList.forEach { it.onSeekComplete(mp) }
        }

        mediaPlayer.setOnCompletionListener { mp ->
            isComplete = true
            controllerList.forEach { it.onCompletion(mp) }
        }

        mediaPlayer.setOnVideoSizeChangedListener { mp, width, height, sar_num, sar_den ->
            renderView.setVideoSize(width, height, sar_num, sar_den)
            controllerList.forEach { it.onVideoSizeChanged(width, height, sar_num, sar_den) }
        }

        mediaPlayer.setOnErrorListener { mp, what, extra ->
            isError = true
            controllerList.forEach { it.onError(mp, what, extra, Exception("null")) }
            true
        }

        gestureHelper = GestureDetectorHelper(
            context,
            this,
            binding.playerRoot,
            binding.playerGesLayer,
            object : IJKGestureListener {
                override fun onScaleBegin(detector: ScaleGestureDetector) {
                    controllerList.forEach { it.onScaleBegin(detector) }
                }

                override fun onScale(detector: ScaleGestureDetector) {
                    controllerList.forEach { it.onScale(detector) }
                }

                override fun onScaleEnd(detector: ScaleGestureDetector) {
                    controllerList.forEach { it.onScaleEnd(detector) }
                }

                override fun onDown(e: MotionEvent): Boolean {
                    controllerList.forEach { it.onDown(e) }
                    return true
                }

                override fun onShowPress(e: MotionEvent) {
                    controllerList.forEach { it.onShowPress(e) }
                }

                override fun onSingleTapUp(e: MotionEvent): Boolean {
                    controllerList.forEach { it.onSingleTapUp(e) }
                    return true
                }

                override fun onScroll(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    distanceX: Float,
                    distanceY: Float
                ): Boolean {
                    gestureHelper?.onScrollOperate(e1, e2, distanceX, distanceY)

                    controllerList.forEach {
                        it.onScroll(
                            e1,
                            e2,
                            distanceX,
                            distanceY,
                            gestureHelper?.isScrollLeft() ?: false,
                            gestureHelper?.isScrollRight() ?: false,
                            gestureHelper?.isScrollTop() ?: false,
                            gestureHelper?.isScrollBottom() ?: false
                        )
                    }
                    return true
                }

                override fun onLongPress(e: MotionEvent) {
                    controllerList.forEach { it.onLongPress(e) }
                }

                override fun onFling(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    velocityX: Float,
                    velocityY: Float
                ): Boolean {
                    controllerList.forEach { it.onFling(e1, e2, velocityX, velocityY) }
                    return true
                }

                override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                    controllerList.forEach { it.onSingleTapConfirmed(e) }
                    return true
                }

                override fun onDoubleTap(e: MotionEvent): Boolean {
                    controllerList.forEach { it.onDoubleTap(e) }
                    return true
                }

                override fun onTouchEvent(e: MotionEvent) {
                    controllerList.forEach { it.onTouchEvent(e) }
                }

                override fun onDoubleTapEvent(e: MotionEvent): Boolean {
                    return true
                }
            })
    }

    fun starPrepared() {
        if (mediaPlayer.isPlaying) return
        handlerPost {
            controllerList.forEach { it.onVideoStartBefore() }
            mediaPlayer.reset()
            if (!TextUtils.isEmpty(dataSource.getUrl())) {
                mediaPlayer.dataSource = dataSource.getUrl()
                mediaPlayer.setSurface(mSurface)
                mediaPlayer.prepareAsync()
                isComplete = false
                isStop = false
                isPause = false
                isError = false
                controllerList.forEach { it.onVideoStart() }
            }
        }
    }

    fun play() {
        handlerPost {
            mediaPlayer.start()
            isPause = false
            isStop = false
            isComplete = false
            isError = false
            controllerList.forEach { it.onVideoPlay() }
        }
    }

    fun stop() {
        handlerPost {
            mediaPlayer.stop()
            isPause = false
            isStop = true
            isComplete = false
            isPrepared = false
            controllerList.forEach { it.onVideoStop() }
        }
    }

    fun pause() {
        handlerPost {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                isPause = true
                isStop = false
                controllerList.forEach { it.onVideoPause() }
            }
        }
    }

    fun autoLandAndPortrait() {
        if (!isFullScreen) {
            fullScreen()
        } else {
            exitFullScreen()
        }
    }

    fun fullScreen() {
        if (!isFullScreen) {
            mSystemUiVisibility =
                IjkUtils.getActivity(context)!!.window.decorView.systemUiVisibility

            var vg = parent as ViewGroup
            blockLayoutParams = layoutParams
            blockIndex = vg.indexOfChild(this)
            blockWidth = width
            blockHeight = height

            vg.removeView(this)

            clonePlayer(vg)

            CONTAINER_LIST.add(vg)

            vg =
                IjkUtils.getActivity(context)!!.window.decorView.findViewById<FrameLayout>(android.R.id.content) as ViewGroup

            val fullLayout = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            vg.addView(this, fullLayout)

            //隐藏状态栏
            if (enableStatusBar) {
                IjkUtils.getActivity(context)?.window?.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            }

            //导航栏
            if (enableNavKey) {
                IjkUtils.hideNavKey(context)
            }

            //切换到横屏
            if (!isPortraitFullscreen && resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                IjkUtils.getActivity(context)?.requestedOrientation =
                    ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            }

            isFullScreen = true
            controllerList.forEach { it.onFullScreen() }
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    fun exitFullScreen() {
        if (isFullScreen) {
            isFullScreen = false
            //显示导航栏
            handlerPost("exitFullScreen", 100L) {
                if (CONTAINER_LIST.size > 0) {
                    if (enableNavKey) {
                        IjkUtils.showNavKey(context, mSystemUiVisibility)
                    }
                    val vg =
                        IjkUtils.getActivity(context)!!.window.decorView.findViewById<FrameLayout>(
                            android.R.id.content
                        ) as ViewGroup
                    vg.removeView(this)
                    CONTAINER_LIST.last.removeViewAt(blockIndex)
                    CONTAINER_LIST.last.addView(this, blockIndex, blockLayoutParams)
                    CONTAINER_LIST.pop()

                    //切换到竖屏
                    if (!isPortraitFullscreen && resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        try {
                            IjkUtils.getActivity(context)!!.requestedOrientation =
                                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        } catch (e: Exception) {
                            log("切换竖屏error：$e")
                        }
                    }

                    //显示状态栏
                    if (enableStatusBar) {
                        IjkUtils.getActivity(context)?.window?.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                    }

                    isFullScreen = false
                    controllerList.forEach { it.onExitFullScreen() }
                }
            }
        }
    }

    private fun clonePlayer(vg: ViewGroup) {
        try {
            val player: IJKPlayer =
                this@IJKPlayer.javaClass.getConstructor(Context::class.java)
                    .newInstance(context)
            player.id = id
            player.minimumWidth = blockWidth
            player.minimumHeight = blockHeight
            vg.addView(player, blockIndex, blockLayoutParams)
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InstantiationException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {// 竖屏
            if (isFullScreen) exitFullScreen()
        } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {// 横屏
            if (!isFullScreen) {
                mSystemUiVisibility =
                    IjkUtils.getActivity(context)!!.window.decorView.systemUiVisibility
            }
        }
    }

    private fun handlerPost(tag: String = "exception", duration: Long = 0L, runnable: Runnable) {
        if (duration == 0L) {
            mHandler.post {
                try {
                    runnable.run()
                } catch (e: Exception) {
                    log("$tag:${e.message}")
                    isError = true
                    controllerList.forEach {
                        it.onError(
                            mediaPlayer,
                            IJKState.error_exception,
                            IJKState.error_exception,
                            e
                        )
                    }
                }
            }
        } else {
            mHandler.postDelayed({
                try {
                    runnable.run()
                } catch (e: Exception) {
                    log("$tag:${e.message}")
                    isError = true
                    controllerList.forEach {
                        it.onError(
                            mediaPlayer,
                            IJKState.error_exception,
                            IJKState.error_exception,
                            e
                        )
                    }
                }
            }, duration)
        }
    }

    fun release() {
        try {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
            mediaPlayer.reset()
            mediaPlayer.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        isPrepared = false
        isComplete = false
        isPause = false
        isStop = false
        isError = false
    }

    fun shake() {
        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }

    open fun getControllerCase(): MutableList<ControllerCase> {
        return mutableListOf()
    }

    override fun getIJKScaleType(): IJKScaleType {
        return renderView.getIJKScaleType()
    }

    override fun getVideoWidth(): Int {
        return renderView.getVideoWidth()
    }

    override fun getVideoHeight(): Int {
        return renderView.getVideoHeight()
    }

    override fun getVideoSarNum(): Int {
        return renderView.getVideoSarNum()
    }

    override fun getVideoSarDen(): Int {
        return renderView.getVideoSarDen()
    }

    override fun getRenderView(): View {
        return renderView.getRenderView()
    }

    fun getModel(): IJKModel {
        return dataSource
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : ControllerCase?> getController(caseId: Int): T? {
        val controller = controllerList.find { it.getId() == caseId }
        return controller as T
    }

    fun getRenderType(): Int {
        return renderType
    }

    fun setRenderType(type: Int) {
        this.renderType = type
        renderView = if (type == SURFACE) {
            IJKSurfaceView(context, surfaceListener)
        } else {
            IJKTextureView(context, surfaceListener)
        }
        val params = FrameLayout.LayoutParams(
            LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        params.gravity = Gravity.CENTER
        binding.playerRender.addView(
            renderView.getRenderView(),
            params
        )
        renderView.setOnSurfaceListener(surfaceListener)
    }

    fun isPlaying(): Boolean {
        return mediaPlayer.isPlaying
    }

    fun isVideoPrepared(): Boolean {
        return isPrepared
    }

    fun setVideoComplete(boolVal: Boolean) {
        this.isComplete = boolVal
    }

    fun isVideoComplete(): Boolean {
        return isComplete
    }

    fun isVideFullScreen(): Boolean {
        return isFullScreen
    }

    fun isVideoPause(): Boolean {
        return isPause
    }

    fun isVideoStop(): Boolean {
        return isStop
    }

    fun isVideoError(): Boolean {
        return isError
    }

    fun setDataSource(url: String) {
        setDataSource(IJKModel().apply { this.setUrl(url) })
    }

    fun setDataSource(model: IJKModel) {
        dataSource = model
    }

    override fun setIJKScaleType(scaleType: IJKScaleType) {
        renderView.setIJKScaleType(scaleType)
    }

    fun setEnableNavKey(enable: Boolean) {
        this.enableNavKey = enable
    }

    fun enableStatusBar(enable: Boolean) {
        this.enableStatusBar = enable
    }

    fun setScaleEnable(enable: Boolean) {
        gestureHelper?.setScaleEnable(enable)
    }

    fun setPortraitFullscreen(boolValue: Boolean) {
        this.isPortraitFullscreen = boolValue
    }

    override fun setOnSurfaceListener(listener: OnSurfaceListener) {
        renderView.setOnSurfaceListener(listener)
    }

    override fun initCover(isHigh: Boolean): Bitmap {
        return renderView.initCover(isHigh)
    }

    override fun taskShotPicture(isHigh: Boolean, listener: IJKShotListener) {
        renderView.taskShotPicture(isHigh, listener)
    }

    fun onBackPressed(): Boolean {
        if (isFullScreen) {
            exitFullScreen()
            return true
        }
        return false
    }

    private fun log(m: String) {
        IjkUtils.log("IJKPlayerView", m)
    }

}