package org.wict.box_show

fun getUrl(type:Int, url:String) :String{
    if (type == 1){
        return "http://eh.cqxyy.net$url"
    }
    if (type == 2){
        return "file:///android_asset/index.html?http://eh.cqxyy.net$url"
    }
    return url
}