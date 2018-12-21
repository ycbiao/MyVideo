package com.example.ycb.myvideo

import android.R.attr.*
import android.media.MediaPlayer
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.SeekBar
import kotlinx.android.synthetic.main.activity_main.*
import java.net.URL
import java.util.*
import javax.security.auth.callback.Callback
import kotlin.concurrent.timerTask
import kotlin.math.log10
import android.os.Build
import android.support.annotation.RequiresApi


class MainActivity : AppCompatActivity(){
    var mediaPlayer =  MediaPlayer()
//    val url = "https://dianbo.ysbang.cn/8fb3b3c9vodcq1256960037/5d1d396d5285890783143417625/TkH9f9IuvvUA.mp4"
    val url = "https://dianbo.ysbang.cn/8fb3b3c9vodcq1256960037/15c8746f5285890783285140708/CCbQ3dYAZVQA.mp4"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        val fileDescriptor = assets.openFd("ysbnew.mp4")
//        mediaPlayer.setDataSource(fileDescriptor.fileDescriptor,fileDescriptor.startOffset,fileDescriptor.length)
        mediaPlayer.setDataSource(url)
        surfaceView.holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mediaPlayer.setScreenOnWhilePlaying(true)
        set()
    }


    private fun set() {

        var handler = Handler{
            val duration = mediaPlayer.duration
            val position = mediaPlayer.currentPosition
            if (duration > 0) {
                val pos = seekBar.getMax().times(position).div(duration)
                seekBar.setProgress(pos)
            }
            true
        }

        var timertask = timerTask{
            if(mediaPlayer.isPlaying.and(!seekBar.isPressed)){
                handler.sendEmptyMessage(0)}
        }

        Timer().schedule(timertask,1000,1000)

        surfaceView.holder?.addCallback(object :SurfaceHolder.Callback{
            override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
            }

            override fun surfaceCreated(holder: SurfaceHolder?) {
                mediaPlayer?.setDisplay(surfaceView.holder)
                mediaPlayer.prepareAsync()
            }
        })

        mediaPlayer.setOnPreparedListener {
            tvTotalTime?.setText(mediaPlayer.duration.div(1000).div(60).toString() + ":" + mediaPlayer.duration.div(1000).rem(60).toString())
            mediaPlayer.start()
            mediaPlayer.isLooping = false
        }

        seekBar?.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser){
                    if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        mediaPlayer.seekTo(mediaPlayer.duration.times(progress).div(seekBar!!.max).toLong(),MediaPlayer.SEEK_CLOSEST_SYNC)
                    else
                        mediaPlayer.seekTo(mediaPlayer.duration.times(progress).div(seekBar!!.max))
                    mediaPlayer.start()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        mediaPlayer.setOnBufferingUpdateListener {
                mp, percent -> seekBar.secondaryProgress = percent
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {

        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }
}
