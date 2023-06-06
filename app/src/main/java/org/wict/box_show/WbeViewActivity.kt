package org.wict.box_show

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.core.graphics.TypefaceCompat.clearCache
import androidx.lifecycle.Observer
import org.wict.box_show.mqtt.MainViewModel
import java.util.ResourceBundle.clearCache
import kotlin.math.abs

class WbeViewActivity : AppCompatActivity() {

    private val model = MainViewModel.getModelInstance()
    val operate = model.operate.value?.operationType
    val testUrl:String = "http://www.baidu.com"

    private var urlType:Int ? = null

    lateinit var webView: WebView
    var url: String? = null
    var mClient  = WebViewClient()

    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wbe_view)
        //隐藏顶部状态栏
        supportActionBar?.hide()

        //加载界面传过来的资源
        url = intent.getStringExtra("url")
        webView = findViewById<WebView?>(R.id.webView).apply {
            // 允许 JavaScript 执行
            settings.javaScriptEnabled = true
            // 允许 HTML 5 视频自动播放（默认为 false）
            settings.mediaPlaybackRequiresUserGesture = false

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            }

            settings.javaScriptEnabled = true
            settings.allowFileAccess = true
            settings.builtInZoomControls = true
            settings.setSupportZoom(true)
            settings.displayZoomControls = false //不显示缩放按钮
            settings.domStorageEnabled = true
            //错误适配白屏
//            settings.setUseWideViewPort(true)
//            settings.setLoadWithOverviewMode(true)
//
//            settings.setDefaultFontSize(15)

            // 手动调用 requestFocus() 获取焦点
            requestFocus()
        }


        webView.loadUrl(testUrl!!)

        //观察是否需要跟换加载新资源
        model.resource.observe(this, Observer {
            urlType = model.resource.value?.resourceType
            val filePath = model.resource.value?.filePath
            url = urlType?.let { type -> filePath?.let { path -> getUrl(type, path) } }
            url?.let { it1 -> Log.v("url", it1) }
            if ( url != null ){
                var intent = Intent()
                if (urlType == 1){
                    intent.setClass(this, VideoActivity::class.java)
                    intent.putExtra("url",url)
                    this.startActivity(intent)
                }else{
                    webView.loadUrl(url!!)
                }
            }
        })

        //观察指令，进行模拟翻页效果
        model.operate.observe(this, Observer {
            val operate = model.operate.value?.operationType
            when(operate){
                "prevPage" -> {
                    // 获取WebView当前的滚动位置和实际高度
                    val currentScrollY = webView.scrollY
                    // 获取当前设备的屏幕高度
                    val screenHeight = resources.displayMetrics.heightPixels / resources.displayMetrics.density.toInt()

                    // 获取页面的高度和下一个滚动位置
                    val pageHeight = webView.contentHeight
                    val prevPage = currentScrollY + screenHeight

                    // 如果当前滚动位置小于页面高度，则继续滚动
                    if (prevPage < pageHeight) {
                        // 计算下一次滚动的y坐标并调用scrollTo()方法来滚动WebView
                        webView.scrollTo( prevPage,0)
                    }
                }
                "nextPage" -> {
                    // 获取WebView当前的滚动位置和实际高度
                    val currentScrollY = webView.scrollY
                    // 获取当前设备的屏幕高度
                    val screenHeight = resources.displayMetrics.heightPixels / resources.displayMetrics.density.toInt()

                    // 获取页面的高度和下一个滚动位置
                    val pageHeight = webView.contentHeight
                    val nextPage = currentScrollY + screenHeight

                    // 如果当前滚动位置小于页面高度，则继续滚动
                    if (nextPage < pageHeight) {
                        // 计算下一次滚动的y坐标并调用scrollTo()方法来滚动WebView
                        webView.scrollTo(0, nextPage)
                    }
                }
                else ->{

                }
            }
        })

        webView.clearCache(true)



    }

    override fun onBackPressed() {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finishAffinity()
    }

}