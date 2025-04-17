package io.video.ijkplayer

import java.io.Serializable


/**
 * by JFZ
 * 2025/4/11
 * desc：
 **/
class IJKModel : Serializable {

    private var url = ""

    fun setUrl(url: String) {
        this.url = url
    }

    fun getUrl(): String {
        return url
    }

}