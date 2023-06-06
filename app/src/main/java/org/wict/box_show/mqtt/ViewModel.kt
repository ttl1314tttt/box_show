package org.wict.box_show.mqtt

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson

class MainViewModel : ViewModel() {

    var operate = MutableLiveData(OperateMgs())
    var resource = MutableLiveData(ResourceMgs())

    //实现单例
    companion object{
        @Volatile
        private var instance:MainViewModel? = null
        fun getModelInstance():MainViewModel{
            return instance ?: synchronized(this) {
                instance ?: MainViewModel().also { instance = it }
            }
        }
    }

    fun updateOperate(jsonString:String){
        val currentOperate = Gson().fromJson(jsonString,OperateMgs::class.java)
        operate.postValue(currentOperate)
        Log.i(ContentValues.TAG, "messageOperate: $operate")
    }

    fun updateResource(jsonString: String){
        val currentResource = Gson().fromJson(jsonString,ResourceMgs::class.java)
        resource.postValue(currentResource)
        Log.i(ContentValues.TAG, "messageResource: $resource")
    }
}


data class OperateMgs(
    val dataType:String = "",
    val operationType:String = ""
)


data class ResourceMgs(
    val dataType: String = "",
    val deviceResourceId: String = "",
    val filePath: String? = null,
    val resourceType: Int = 0
)
////实现单例
//companion object{
//    private var instance: MainViewModel? = null
//    @Synchronized
//    fun getModelInstance():MainViewModel{
//        if (instance == null){
//            instance = MainViewModel()
//        }
//        return instance!!
//    }
//}