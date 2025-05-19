package cn.video.ijkplayer

import android.annotation.SuppressLint
import android.app.PictureInPictureParams
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Rational
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cn.video.ijkplayer.databinding.ActivityIjkplayerBinding
import io.video.ijkplayer.scaletype.AutoWrapType
import io.video.ijkplayer.scaletype.CenterCropType
import io.video.ijkplayer.scaletype.NoneType


/**
 * by JFZ
 * 2025/4/17
 * desc：
 **/
class IJKPlayerActivity : AppCompatActivity() {

    private val binding: ActivityIjkplayerBinding by lazy {
        ActivityIjkplayerBinding.inflate(layoutInflater)
    }

//    private val src =
//        "https://mv6.music.tc.qq.com/386301188FE054E754DF5BBABF5CF8D119C70B5A53F3526FAA01E94DCD6CCB30352D3D31D51E303784BB3F1023C7FA9EZZqqmusic_default__v215192fdc/qmmv_0b53cmb4waadgyakmw77hztvoeyazmjqhs2a.f9835.mp4?fname=qmmv_0b53cmb4waadgyakmw77hztvoeyazmjqhs2a.f9835.mp4"

    private val src =
        "https://mv6.music.tc.qq.com/B8B6878D26EEF1FA9BC0FBD9491D651768FE7746D3421B29EBF7C85B15CF63444D97EC7312347126CE4AB1CDF274FF7AZZqqmusic_default__v21ea0f986/qmmv_0b53ruaaaaaai4aarhl6brrvjdiaacgqaaca.f9835.mp4?fname=qmmv_0b53ruaaaaaai4aarhl6brrvjdiaacgqaaca.f9835.mp4"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.rg.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb1 -> binding.video.setIJKScaleType(NoneType())
                R.id.rb2 -> binding.video.setIJKScaleType(CenterCropType())
                R.id.rb3 -> binding.video.setIJKScaleType(AutoWrapType())
            }
        }

        binding.rg.check(R.id.rb1)
        binding.video.bindLifecycle(this)
        binding.video.setDataSource(src)

        binding.start.setOnClickListener {
            binding.video.starPrepared()
        }

        binding.play.setOnClickListener {
            binding.video.play()
        }

        binding.stop.setOnClickListener {
            binding.video.stop()
        }

        binding.pause.setOnClickListener {
            binding.video.pause()
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        if (!binding.video.onBackPressed()) finish()
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.e("onConfigurationChanged", newConfig.toString())
    }
}