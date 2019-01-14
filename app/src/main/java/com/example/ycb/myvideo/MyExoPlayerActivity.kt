package com.example.ycb.myvideo

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.database.Cursor
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.support.constraint.ConstraintLayout
import android.support.v7.app.AppCompatActivity
import android.view.*
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import com.example.ycb.myvideo.R.id.vseekbar_volume
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Log
import kotlinx.android.synthetic.main.exoplayer_activity.*
import kotlinx.android.synthetic.main.mediaplayer_activity.*
import java.io.File
import java.net.URI
import java.util.*
import java.util.logging.Logger
import kotlin.concurrent.timerTask

/**
 * Created by biao on 2018/12/24.
 */
class MyExoPlayerActivity : AppCompatActivity(),OnOperationListener{

    private var isStart = false

    private var isFull = false

    private lateinit var mPlayer : SimpleExoPlayer

    private val REQUEST_CODE_ASK_PERMISSIONS = 0x12

    private  lateinit var mGestureDetector : GestureDetector

    private  lateinit var mAudioManager : AudioManager
    /** 当前声音  */
    private  var mCurrentVolume : Int = 0

    /** 最大声音  */
    private var mMaxVolume: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.exoplayer_activity)
        val trackSelector = DefaultTrackSelector()
        val renderersFactory = DefaultRenderersFactory(this)
        mPlayer = ExoPlayerFactory.newSimpleInstance(this, renderersFactory, trackSelector)
        exo_surfaceV.player = mPlayer
        mPlayer.addListener(object : Player.EventListener {
            override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {

            }

            override fun onLoadingChanged(isLoading: Boolean) {
                Log.i(this@MyExoPlayerActivity.toString(), "is loading $isLoading")
            }

            override fun onSeekProcessed() {
            }

            override fun onPlayerError(error: ExoPlaybackException?) {
                Toast.makeText(this@MyExoPlayerActivity,error.toString(),Toast.LENGTH_LONG).show()
            }

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if(playbackState == Player.STATE_ENDED){
                    mPlayer.seekTo(0)
                    seekBar_exo.progress = 0
                    stopPlay()
                }
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
                startPlay()
            }else{
                stopPlay()
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

        btn_test_url.setOnClickListener {
            val dataSourceFactory = DefaultHttpDataSourceFactory("ycb.myvideo")
            val videoSource = ExtractorMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.parse(getString(R.string.url_video_test)))
            mPlayer.prepare(videoSource)
            startPlay()
        }

        btn_local_file.setOnClickListener {
            if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                var hasReadPermission = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                if (hasReadPermission != PackageManager.PERMISSION_GRANTED) {
                         requestPermissions(
                             arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE_ASK_PERMISSIONS);
                }else{
                    val intent : Intent = Intent(Intent.ACTION_GET_CONTENT)
                    intent.setType("video/*")
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    startActivityForResult(intent,1)
                }
            }
        }
        setVolume()
    }

    private fun setVolume() {
        vseekbar_volume.max = resources.getDimensionPixelOffset(R.dimen.video_height)
        mGestureDetector = GestureDetector(this,MyGestureListener())
        mAudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        mMaxVolume = mAudioManager
            .getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        mCurrentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        vseekbar_volume.progress = vseekbar_volume.max.times(mCurrentVolume.div(mMaxVolume.toFloat())).toInt()
        vseekbar_volume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (seekBar != null && seekBar.isPressed) {
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,mMaxVolume.times(progress.div(seekBar.max.toFloat())).toInt(),0)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })

        exo_surfaceV.setOnTouchListener { v, event ->
            if(mGestureDetector.onTouchEvent(event)){
                return@setOnTouchListener true
            }else
                super.onTouchEvent(event)
        }
    }

    //    override fun onTouchEvent(event: MotionEvent?): Boolean {
//        if(mGestureDetector.onTouchEvent(event)){
//            return true
//        }
//        return super.onTouchEvent(event)
//    }

    override fun startPlay() {
        mPlayer.playWhenReady = true
        start_exo.text = "暂停"
        isStart = true
    }

    override fun stopPlay() {
        isStart = false
        start_exo.text = "开始"
        mPlayer.playWhenReady = false
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

    inner class MyGestureListener : GestureDetector.SimpleOnGestureListener(){
        override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
            Toast.makeText(this@MyExoPlayerActivity, distanceY.toString(),Toast.LENGTH_LONG).show()
            vseekbar_volume.isPressed = true
            vseekbar_volume.progress += distanceY.toInt()
            return true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            val uri = data!!.data
            if(uri != null){
                var proj = arrayOf(MediaStore.Images.Media.DATA)
                var actualimagecursor = managedQuery(uri, proj, null, null, null);
                val actual_image_column_index = actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                actualimagecursor.moveToFirst()
                val video_path = actualimagecursor.getString(actual_image_column_index)
                val file = File(video_path)
                Toast.makeText(this@MyExoPlayerActivity, file.toString(), Toast.LENGTH_SHORT).show();
                val dataSourceFactory = DefaultDataSourceFactory(this,"ycb.myvideo")
                val videoSource = ExtractorMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(Uri.parse("" + video_path))

                mPlayer.prepare(videoSource)
                startPlay()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            REQUEST_CODE_ASK_PERMISSIONS -> {
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    val intent : Intent = Intent(Intent.ACTION_GET_CONTENT)
                    intent.setType("video/*")
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    startActivityForResult(intent,1)
                }else{
                    // Permission Denied
                    Toast.makeText(this@MyExoPlayerActivity, "READ_EXTERNAL_STORAGE Denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        when(event?.keyCode){
            KeyEvent.KEYCODE_VOLUME_UP,KeyEvent.KEYCODE_VOLUME_DOWN ->{
                vseekbar_volume.isPressed = false
                mCurrentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                vseekbar_volume.progress = vseekbar_volume.max.times(mCurrentVolume.div(mMaxVolume.toFloat())).toInt()
            }
        }
        return super.dispatchKeyEvent(event)
    }
}