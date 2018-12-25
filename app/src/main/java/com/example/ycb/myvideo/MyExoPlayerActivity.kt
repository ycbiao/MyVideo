package com.example.ycb.myvideo

import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Log
import kotlinx.android.synthetic.main.exoplayer_activity.*
import java.util.*

/**
 * Created by biao on 2018/12/24.
 */
class MyExoPlayerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.exoplayer_activity)
        val trackSelector = DefaultTrackSelector()
        val renderersFactory = DefaultRenderersFactory(this)
        val mPlayer = ExoPlayerFactory.newSimpleInstance(this, renderersFactory, trackSelector)
        exo_surfaceV.player = mPlayer
        val dataSourceFactory = DefaultHttpDataSourceFactory("ysbang.cn")
        val videoSource = ExtractorMediaSource.Factory(dataSourceFactory)
            .createMediaSource(Uri.parse(getString(R.string.url_video_test)))
        mPlayer.prepare(videoSource)
        mPlayer.addListener(object : Player.EventListener {
            override fun onLoadingChanged(isLoading: Boolean) {
                Log.i(this@MyExoPlayerActivity.toString(), "is loading $isLoading")
            }
        })
    }
}