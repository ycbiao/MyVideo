package com.example.ycb.myvideo

import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.view.SurfaceHolder
import android.widget.SeekBar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.mediaplayer_activity.*
import java.util.*
import kotlin.concurrent.timerTask

/**
 * Created by biao on 2018/12/25.
 */
class MyMediaPlayerActivity : AppCompatActivity() {
    var mediaPlayer =  MediaPlayer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mediaplayer_activity)
//        val fileDescriptor = assets.openFd("ysbnew.mp4")
//        mediaPlayer.setDataSource(fileDescriptor.fileDescriptor,fileDescriptor.startOffset,fileDescriptor.length)
        mediaPlayer.setDataSource(getString(R.string.url_video_test))
        surfaceView_media.holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mediaPlayer.setScreenOnWhilePlaying(true)
        set()
    }


    private fun set() {

        var handler = Handler {
            val duration = mediaPlayer.duration
            val position = mediaPlayer.currentPosition
            if (duration > 0) {
                val pos = seekBar_media.getMax().times(position).div(duration)
                seekBar_media.setProgress(pos)
            }
            true
        }

        var timertask = timerTask {
            if (mediaPlayer.isPlaying.and(!seekBar_media.isPressed)) {
                handler.sendEmptyMessage(0)
            }
        }

        Timer().schedule(timertask, 1000, 1000)

        surfaceView_media.holder?.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
            }

            override fun surfaceCreated(holder: SurfaceHolder?) {
                mediaPlayer?.setDisplay(surfaceView_media.holder)
                mediaPlayer.prepareAsync()
            }
        })

        mediaPlayer.setOnPreparedListener {
            tvTotalTime_media?.text =
                mediaPlayer.duration.div(1000).div(60).toString() + ":" + mediaPlayer.duration.div(1000).rem(
                    60)
                .toString()

            mediaPlayer.start()
            mediaPlayer.isLooping = false
        }

        seekBar_media?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        mediaPlayer.seekTo(
                            mediaPlayer.duration.times(progress).div(seekBar!!.max).toLong(),
                            MediaPlayer.SEEK_CLOSEST_SYNC
                        )
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
        mediaPlayer.setOnBufferingUpdateListener { mp, percent ->
            seekBar_media.secondaryProgress = percent
        }
    }
}