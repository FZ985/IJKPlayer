package io.video.ijkplayer.scaletype

import android.content.Context
import android.view.View


/**
 * by JFZ
 * 2025/4/10
 * desc：居中裁剪
 **/
class CenterCropType : IJKScaleType {
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
        val parentWidth = View.MeasureSpec.getSize(widthMeasureSpec)
        var parentHeight = View.MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)

        if (videoWidth == 0 || videoHeight == 0) {
            width = parentWidth
            height = parentHeight
            val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)
            if (heightMode == View.MeasureSpec.UNSPECIFIED) {
                // ScrollView 内部可能是这个
                if (parentHeight == 0) {
                    this.height = defaultHeight(context)
                }
            }
        } else {
            var vWidth = videoWidth
            if (sarNum > 0 && sarDen > 0) {
                vWidth = videoWidth * sarNum / sarDen
            }
            if (heightMode == View.MeasureSpec.UNSPECIFIED) {
                // ScrollView 内部可能是这个
                if (parentHeight == 0) {
                    parentHeight = defaultHeight(context)
                }
            }

            val viewAspect = parentWidth.toFloat() / parentHeight
            val videoAspect = vWidth.toFloat() / videoHeight

            val finalWidth: Int
            val finalHeight: Int

            if (videoAspect < viewAspect) {
                // 视频太高，按照宽度放大，裁剪顶部和底部
                finalWidth = parentWidth
                finalHeight = (parentWidth / videoAspect).toInt()
            } else {
                // 视频太宽，按照高度放大，裁剪左右
                finalHeight = parentHeight
                finalWidth = (parentHeight * videoAspect).toInt()
            }
            width = finalWidth
            height = finalHeight
        }
    }

    override fun getWidth(): Int {
        return width
    }

    override fun getHeight(): Int {
        return height
    }
}