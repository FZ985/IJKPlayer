package io.video.ijkplayer.scaletype

import android.content.Context
import android.view.View


/**
 * by JFZ
 * 2025/4/10
 * desc：根据视频宽高自适应
 **/
class AutoWrapType : IJKScaleType {

    private var width = 0
    private var height = 0
    override fun prepareMeasure(
        context: Context,
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
        videoWidth: Int,
        videoHeight: Int,
        sarNum: Int,
        sarDen: Int,
        rotate: Int
    ) {
        val viewWidth = View.MeasureSpec.getSize(widthMeasureSpec)
        var viewHeight = View.MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)

        if (videoWidth == 0 || videoHeight == 0) {
            // 如果没设置视频尺寸，默认拉伸填满
            width = videoWidth
            height = viewHeight

            if (heightMode == View.MeasureSpec.UNSPECIFIED) {
                // ScrollView 内部可能是这个
                if (viewHeight == 0) {
                    if (videoHeight != 0) {
                        this.height = videoHeight
                    } else {
                        this.height = defaultHeight(context)
                    }
                }
            }
        } else {
            var vWidth = videoWidth
            if (sarNum > 0 && sarDen > 0) {
                vWidth = videoWidth * sarNum / sarDen
            }
            if (heightMode == View.MeasureSpec.UNSPECIFIED) {
                // ScrollView 内部可能是这个
                if (viewHeight == 0) {
                    viewHeight = defaultHeight(context)
                }
            }

            val aspectRatio = vWidth.toFloat() / videoHeight
            val newWidth: Int
            val newHeight: Int
            if (viewWidth / viewHeight.toFloat() > aspectRatio) {
                // 宽比过大 → 以高度为基准，调整宽度
                newHeight = viewHeight
                newWidth = (viewHeight * aspectRatio).toInt()
            } else {
                // 高比过大 → 以宽度为基准，调整高度
                newWidth = viewWidth
                newHeight = (viewWidth / aspectRatio).toInt()
            }
            width = newWidth
            height = newHeight
        }
    }

    override fun getWidth(): Int {
        return width
    }

    override fun getHeight(): Int {
        return height
    }
}