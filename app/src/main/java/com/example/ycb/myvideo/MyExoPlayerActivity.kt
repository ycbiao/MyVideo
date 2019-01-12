package com.example.ycb.myvideo

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.constraint.ConstraintLayout
import android.support.v7.app.AppCompatActivity
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.TextView
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Log
import kotlinx.android.synthetic.main.exoplayer_activity.*
import kotlinx.android.synthetic.main.mediaplayer_activity.*
import java.util.*
import kotlin.concurrent.timerTask

/**
 * Created by biao on 2018/12/24.
 */
class MyExoPlayerActivity : AppCompatActivity() {

    private var isStart = false

    private var isFull = false

    private lateinit var mPlayer : SimpleExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.exoplayer_activity)
        val trackSelector = DefaultTrackSelector()
        val renderersFactory = DefaultRenderersFactory(this)
        mPlayer = ExoPlayerFactory.newSimpleInstance(this, renderersFactory, trackSelector)
        exo_surfaceV.player = mPlayer
        val dataSourceFactory = DefaultHttpDataSourceFactory("ysbang.cn")
        val videoSource = ExtractorMediaSource.Factory(dataSourceFactory)
            .createMediaSource(Uri.parse(getString(R.string.url_video_test)))
        mPlayer.prepare(videoSource)
        mPlayer.addListener(object : Player.EventListener {
            override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {

            }

            override fun onLoadingChanged(isLoading: Boolean) {
                Log.i(this@MyExoPlayerActivity.toString(), "is loading $isLoading")
            }

            override fun onSeekProcessed() {
            }
        })

        var handler = Handler {
            seekBar_exo.max = mPlayer.duration.toInt()
            seekBar_exo.progress = mPlayer.contentPosition.toInt()
            seekBar_exo.secondaryProgress = mPlayer.bufferedPosition.toInt()
            time.text = mPlayer.contentPosition.div(1000).div(60).toString() + ":" + mPlayer.contentPosition.div(1000).rem(60).toString() +
                    "/" + mPlayer.duration.div(1000).div(60).toString() + ":" + mPlayer.duration.div(1000).rem(60).toString()
            true
        }


        val timerTask : TimerTask = timerTask {
            if (isStart.and(!seekBar_exo.isPressed)) {
                handler.sendEmptyMessage(0)
            }
        }

        Timer().schedule(timerTask, 1000, 1000)

        seekBar_exo?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mPlayer.seekTo(mPlayer.duration.times(progress).div(seekBar!!.max))
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        start_exo.setOnClickListener {
            if(!isStart){
                mPlayer.playWhenReady = true
                (it as TextView).text = "暂停"
                isStart = true
            }else{
                isStart = false
                (it as TextView).text = "开始"
                mPlayer.playWhenReady = false
            }
        }

        tv_exo_full.setOnClickListener {
            if(!isFull){
                val flagBack =  WindowManager.LayoutParams.FLAG_FULLSCREEN
                val windowBack : Window = window
                windowBack.setFlags(flagBack,flagBack)
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                (it as TextView).text = "小屏"
                isFull = true
            }else{
                isFull = false
                (it as TextView).text = "全屏"
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        }
    }

    override fun onStop() {
        super.onStop()
        mPlayer.playWhenReady = false
        isStart = false
    }

    override fun onRestart() {
        super.onRestart()
        mPlayer.playWhenReady = true
        isStart = true
    }

    override fun onBackPressed() {
        if(isFull){
            isFull = false
            tv_exo_full.text = "全屏"
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }else
        super.onBackPressed()

    }

    override fun onDestroy() {
        super.onDestroy()
        mPlayer.release()
    }
    override fun onConfigurationChanged(newConfig: Configuration?) {
        if(this.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            val params : ConstraintLayout.LayoutParams = exo_surfaceV.layoutParams as ConstraintLayout.LayoutParams
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
            params.height = ViewGroup.LayoutParams.MATCH_PARENT
            exo_surfaceV.layoutParams = params
        }else if(this.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT){
            val flagBack =  WindowManager.LayoutParams.FLAG_FULLSCREEN
            val windowBack : Window = window
            windowBack.clearFlags(flagBack)
            val params : ConstraintLayout.LayoutParams = exo_surfaceV.layoutParams as ConstraintLayout.LayoutParams
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
            params.height = resources.getDimensionPixelOffset(R.dimen.video_height)
            exo_surfaceV.layoutParams = params
        }
        super.onConfigurationChanged(newConfig)
    }
}