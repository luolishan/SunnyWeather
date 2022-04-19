package com.sunnyweather.android.logic.network

import com.sunnyweather.android.SunnyWeatherApplication
import com.sunnyweather.android.logic.model.PlaceResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface PlaceService {

    //GET请求用于从服务器获取数据
    //POST请求用于向服务器提交数据
    //PUT和PATCH请求用于修改服务器上的数据
    //DELETE请求用于删除服务器上的数据


    //https://api.caiyunapp.com/v2/place?query=北京&token={token}&lang=zh_CN
    @GET("v2/place?token=${SunnyWeatherApplication.TOKEN}&lang=zh_CN")
    // 对带参数的GET请示，可以用 @Query 在实现在URL后面加上参数
    fun searchPlaces(@Query("query") query: String): Call<PlaceResponse>

}