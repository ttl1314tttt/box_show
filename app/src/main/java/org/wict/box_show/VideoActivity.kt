package org.wict.box_show

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.VideoView
import androidx.lifecycle.Observer
import org.wict.box_show.mqtt.MainViewModel
import java.net.URL

class VideoActivity : AppCompatActivity() {

    private val model = MainViewModel.getModelInstance()
    val operate = model.operate.value?.operationType
    private var urlType:Int ? = null

    lateinit var videoView: VideoView

    var url : String? = null
    lateinit var path : Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)
        //隐藏顶部状态栏
        supportActionBar?.hide()


        videoView = findViewById(R.id.videoView)
        url = intent.getStringExtra("url")
        path = Uri.parse(url)
        videoView.setVideoURI(path)

        videoView.setOnPreparedListener{
            videoView.start()
        }

        videoView.setOnCompletionListener { mp ->
            // 视频播放完成后的操作
        }

        videoView.setOnErrorListener { mp, what, extra ->
            // 视频播放出错时的操作
            false
        }

        //观察是否需要跟换加载新资源
        model.resource.observe(this, Observer {
            urlType = model.resource.value?.resourceType
            val filePath = model.resource.value?.filePath
            url = urlType?.let { type -> filePath?.let { path -> getUrl(type, path) } }
            url?.let { it1 -> Log.v("url", it1) }
            if ( url != null ){
                var intent = Intent()
                if (urlType == 1){
                    videoView.setVideoURI(Uri.parse(url))
                }else{
                    intent.setClass(this, WbeViewActivity::class.java)
                    intent.putExtra("url",url)
                    this.startActivity(intent)
                }
            }
        })

        //观察操作指令
        model.operate.observe(this, Observer {
            val operate = model.operate.value?.operationType
            when(operate){
                "play" -> {
                    videoView.start()
                }
                "suspend" -> {
                    videoView.pause()
                }
                else ->{

                }
            }
        })

    }

    override fun onBackPressed() {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finishAffinity()
    }
}