package io.video.ijkplayer.controller

import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isVisible
import io.video.ijkplayer.R
import io.video.ijkplayer.databinding.IjkControllerErrorBinding
import io.video.ijkplayer.view.IJKPlayer
import tv.danmaku.ijk.media.player.IMediaPlayer
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import kotlin.math.absoluteValue


/**
 * by JFZ
 * 2025/4/17
 * desc：播放错误相关操作
 **/
class PlayErrorController : ControllerCase {
    companion object {
        @JvmStatic
        val ID = "PlayErrorController".hashCode().absoluteValue
    }

    private lateinit var player: IJKPlayer
    private lateinit var mediaPlayer: IjkMediaPlayer

    private var barController: TopBottomBarController? = null

    private val binding: IjkControllerErrorBinding by lazy {
        IjkControllerErrorBinding.inflate(LayoutInflater.from(player.context))
    }

    private val controllerView: FrameLayout by lazy {
        player.findViewById(R.id.player_controller)
    }

    override fun onCreate(parentView: IJKPlayer, mp: IjkMediaPlayer) {
        this.player = parentView
        this.mediaPlayer = mp
        binding.errorButton.setOnClickListener {
            binding.loading.isVisible = true
            binding.loading.show()
            binding.errorButton.isVisible = false
            player.postDelayed({
                player.starPrepared()
            }, 800L)
        }
    }

    override fun onAllCaseCreated(parentView: IJKPlayer, mp: IjkMediaPlayer) {
        barController = parentView.getController(TopBottomBarController.ID)
    }

    override fun onError(mp: IMediaPlayer, what: Int, extra: Int, e: Exception) {
        barController?.setVisibilityPlayButton(false)
        controllerView.removeView(binding.root)
        controllerView.addView(binding.root)
        binding.root.postDelayed({
            binding.loading.isVisible = false
            binding.errorButton.isVisible = true
            binding.errorButton.text = player.context.resources.getString(
                R.string.ijk_player_error,
                what.toString()
            )
        }, 250L)
    }

    override fun onPrepared(mp: IMediaPlayer) {
        binding.loading.hide()
        binding.loading.isVisible = false
        controllerView.removeView(binding.root)
    }

    override fun reset() {
    }

    override fun getId() = PlayErrorController.ID
}