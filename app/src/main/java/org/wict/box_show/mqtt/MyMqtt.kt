package org.wict.box_show.mqtt

import android.content.ContentValues.TAG
import android.content.Context
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*

class MyMqtt(context: Context) {

    val model= MainViewModel.getModelInstance()

    val serverUri:String = "tcp://eh.cqxyy.net:1883"
    val clientId:String = "tvtv"
    val topic:String = "/my/eh/$clientId/user/msg"
    var mClient: MqttAndroidClient? = null
    val context = context

    val testString = "我在发消息"


    fun initMqtt(){
        val mClient = createClient()
        val mOptions = createOptions()
        //建立链接
        try {
            mClient?.connect(mOptions, context, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.v("success", "onSuccess:连接成功 ")
                    showToast("$clientId")
                    try {
                        mClient?.subscribe(topic, 0)
                    } catch (e: MqttException) {
                        Log.e(TAG, "onCreate: ", e)
                    }

                    try {
                        var str = testString
                        var msg = MqttMessage()
                        msg.payload =str.toByteArray()
                        mClient?.publish(topic,msg)
                    } catch (e: MqttException) {
                        Log.e(TAG, "onCreate: ",e )
                    }
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.v("failure", "onFailure: " + exception?.message)
                    showToast("连接失败")

                }

            })
        } catch (e: MqttException) {
            Log.e(TAG, "onCreate: ", e)
        }

    }

    private fun createClient(): MqttAndroidClient? {
        //1、创建接口回调
        //以下回调都在主线程中(如果使用MqttClient,使用此回调里面的都是非主线程)
        val mqttCallback: MqttCallbackExtended = object : MqttCallbackExtended {
            override fun connectComplete(reconnect: Boolean, serverURI: String) {
                //连接成功
                Log.i(TAG, "connectComplete: ")
                showToast("连接成功")
            }

            override fun connectionLost(cause: Throwable) {
                //断开连接
                Log.i(TAG, "connectionLost: ")
                showToast("断开连接")

            }

            @Throws(Exception::class)
            override fun messageArrived(topic: String, message: MqttMessage) {
                //得到的消息
                val msg = message.payload
                val jsonString = String(msg)
                Log.i(TAG, "messageArrived: $jsonString")
                showToast("接收到的消息为：$jsonString")
                if (jsonString != testString){
                    if (isResource(jsonString)){
                        //存资源消息
                        model.updateResource(jsonString)
                    }else{
                        //存操作消息
                        model.updateOperate(jsonString)
                    }
                }
            }

            override fun deliveryComplete(token: IMqttDeliveryToken) {
                //发送消息成功后的回调
                Log.i(TAG, "deliveryComplete: ")
                showToast("发送成功")
            }
        }

        //2、创建Client对象
        try {
            mClient = MqttAndroidClient(context,serverUri,clientId)
            mClient?.setCallback(mqttCallback) //设置回调函数
        } catch (e: MqttException) {
            Log.e(TAG, "createClient: ", e)
        }

        return mClient
    }

    private fun createOptions(): MqttConnectOptions {
        val mOptions = MqttConnectOptions()
        mOptions.isAutomaticReconnect = true //断开后，是否自动连接
        mOptions.isCleanSession = true //是否清空客户端的连接记录。若为true，则断开后，broker将自动清除该客户端连接信息
        mOptions.connectionTimeout = 60 //设置超时时间，单位为秒
        mOptions.userName = "dev" //设置用户名。跟Client ID不同。用户名可以看做权限等级
        mOptions.password = "123456".toCharArray() //设置登录密码
        mOptions.keepAliveInterval = 60 //心跳时间，单位为秒。即多长时间确认一次Client端是否在线
        mOptions.maxInflight = 10 //允许同时发送几条消息（未收到broker确认信息）
        mOptions.mqttVersion = MqttConnectOptions.MQTT_VERSION_3_1_1 //选择MQTT版本

        return mOptions
    }

    private fun showToast(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
    }

    //判断消息类型，资源消息返回真，操作反之
    private fun isResource(jsonString: String):Boolean{
        val obj = Gson().fromJson(jsonString,JsonObject::class.java)
        val dataType = obj.get("dataType").asString
        Log.v("dateType",dataType)
        return dataType == "resourceSend"
    }

}