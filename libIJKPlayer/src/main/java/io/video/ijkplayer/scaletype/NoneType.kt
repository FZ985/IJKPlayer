package io.video.ijkplayer.scaletype

import android.content.Context
import android.view.View


/**
 * by JFZ
 * 2025/4/10
 * desc：跟随父容器宽高，可能会 拉伸，压扁
 **/
class NoneType : IJKScaleType {

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
        val parentHeight = View.MeasureSpec.getSize(heightMeasureSpec)

        this.width = parentWidth
        this.height = parentHeight

        val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)

//        if (heightMode == View.MeasureSpec.AT_MOST) {
//            // wrap_content 情况
//            Log.e("CustomView", "height is wrap_content：$heightSize")
//        } else if (heightMode == View.MeasureSpec.EXACTLY) {
//            // match_parent or 固定大小
//            Log.e("CustomView", "height is match_parent or exact：$heightSize")
//        } else
        if (heightMode == View.MeasureSpec.UNSPECIFIED) {
            // ScrollView 内部可能是这个
            if (parentHeight == 0) {
                this.height = defaultHeight(context)
            }
        }
    }

    override fun getWidth(): Int {
        return width
    }

    override fun getHeight(): Int {
        return height
    }
}