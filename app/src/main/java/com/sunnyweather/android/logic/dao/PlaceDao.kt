package com.sunnyweather.android.logic.dao

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson
import com.sunnyweather.android.SunnyWeatherApplication
import com.sunnyweather.android.logic.model.Place

object PlaceDao {

    // 存储数据-将Place对象存储到SharedPreferences文件中
    fun savePlace(place: Place) {
        sharedPreferences().edit {
            // 通过GSON将Place对象转成一个JSON字符串，然后就可以用字符串存储的方式来保存数据了
            putString("place", Gson().toJson(place))
        }
    }

    // 读取数据的接口-将Place对象读取出来
    fun getSavedPlace(): Place {
        // 将JSON字符串从SharedPreferences文件中读取出来
        val placeJson = sharedPreferences().getString("place", "")
        // 再通过GSON将JSON字符串解析成Place对象并返回
        return Gson().fromJson(placeJson, Place::class.java)
    }

    // 用于判断是否有数据已被存储
    fun isPlaceSaved() = sharedPreferences().contains("place")

    private fun sharedPreferences() = SunnyWeatherApplication.context.getSharedPreferences("sunny_weather", Context.MODE_PRIVATE)

}