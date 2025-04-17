package io.video.ijkplayer.scaletype

import android.content.Context
import io.video.ijkplayer.R


/**
 * by JFZ
 * 2025/4/10
 * desc：
 **/
interface IJKScaleType {

    fun prepareMeasure(
        context: Context,
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
        videoWidth: Int,
        videoHeight: Int,
        sarNum: Int,
        sarDen: Int,
        rotate: Int
    )

    fun getWidth(): Int

    fun getHeight(): Int

    fun defaultHeight(context: Context): Int {
        return context.resources.getDimensionPixelOffset(R.dimen.ijk_player_default_height)
    }
}