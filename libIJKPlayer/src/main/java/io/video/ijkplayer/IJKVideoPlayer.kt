package io.video.ijkplayer

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import io.video.ijkplayer.controller.ControllerCase
import io.video.ijkplayer.controller.FixFullScreenPauseController
import io.video.ijkplayer.controller.PlayErrorController
import io.video.ijkplayer.controller.TopBottomBarController
import io.video.ijkplayer.view.IJKPlayer


/**
 * by JFZ
 * 2025/4/11
 * desc：
 **/
class IJKVideoPlayer : IJKPlayer {
    constructor(context: Context) : super(context) {
    }

    constructor(context: Context, attr: AttributeSet?) : super(context, attr) {
    }

    constructor(context: Context, attr: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attr,
        defStyleAttr
    )

    override fun onInit() {
        super.onInit()
        setBackgroundColor(Color.BLACK)
    }

    override fun getControllerCase(): MutableList<ControllerCase> {
        return mutableListOf(
            //UI控制器，不需要则删掉
            TopBottomBarController(),
            //暂停时，全屏切换的组件黑屏问题
            FixFullScreenPauseController(),
            //播放错误控制
            PlayErrorController()
        )
    }

}