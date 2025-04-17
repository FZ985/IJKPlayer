package io.video.ijkplayer.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.util.Log
import android.view.View


/**
 * by JFZ
 * 2025/4/14
 * desc：
 **/
object IjkUtils {

    @JvmStatic
    val enableLog = true

    fun getActivity(context: Context): Activity? {
        return when (context) {
            is Activity -> context as Activity
            is ContextWrapper -> getActivity((context as ContextWrapper).baseContext)
            else -> null
        }
    }

    fun hideNavKey(context: Context) {
        getActivity(context)?.let {
            if (Build.VERSION.SDK_INT >= 29) {
                //       设置屏幕始终在前面，不然点击鼠标，重新出现虚拟按键
                it.window.decorView.systemUiVisibility =
                    (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav
                            // bar
                            or View.SYSTEM_UI_FLAG_IMMERSIVE)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                //       设置屏幕始终在前面，不然点击鼠标，重新出现虚拟按键
                it.window.decorView.systemUiVisibility =
                    (View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav
                            // bar
                            or View.SYSTEM_UI_FLAG_IMMERSIVE)
            } else {
                it.window.decorView.systemUiVisibility = (
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav
                        )
            }
        }
    }

    fun showNavKey(context: Context, systemUiVisibility: Int) {
        getActivity(context)?.let {
            it.window.decorView.systemUiVisibility =
                systemUiVisibility
        }
    }


    fun log(tag: String, msg: String) {
        if (enableLog) {
            Log.e(tag, msg)
        }
    }
}