package com.sunnyweather.android.logic.network

import com.sunnyweather.android.SunnyWeatherApplication
import com.sunnyweather.android.logic.model.DailyResponse
import com.sunnyweather.android.logic.model.RealtimeResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface WeatherService {

    //GET请求用于从服务器获取数据
    //POST请求用于向服务器提交数据
    //PUT和PATCH请求用于修改服务器上的数据
    //DELETE请求用于删除服务器上的数据

    // 获取实时的天气信息
    // https://api.caiyunapp.com/v2.5/{token}/116.4073963,39.9041999/realtime.json
    @GET("v2.5/${SunnyWeatherApplication.TOKEN}/{lng},{lat}/realtime.json")
    // 使用了@Path注解来向请求接口中动态传入经纬度的坐标
    fun getRealtimeWeather(@Path("lng") lng: String, @Path("lat") lat: String): Call<RealtimeResponse>


    // 获取未来几天的天气信息
    // https://api.caiyunapp.com/v2.5/{token}/116.4073963,39.9041999/daily.json
    @GET("v2.5/${SunnyWeatherApplication.TOKEN}/{lng},{lat}/daily.json")
    // 使用了@Path注解来向请求接口中动态传入经纬度的坐标
    fun getDailyWeather(@Path("lng") lng: String, @Path("lat") lat: String): Call<DailyResponse>
}