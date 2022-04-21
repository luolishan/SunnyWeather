package com.sunnyweather.android.logic.model

import com.google.gson.annotations.SerializedName

// 数据模型类
data class RealtimeResponse(val status: String, val result: Result) {

    data class Result(val realtime: Realtime)

    data class Realtime(
        val skycon: String, val temperature: Float,
        @SerializedName("air_quality") val airQuality: AirQuality
    )

    /* 由于JSON中一些字段的命名可能与Kotlin的命名规范不太一致，
    因此这里使用了@SerializedName注解的方式，来让JSON字段和Kotlin字段之间建立映射关系 */

    data class AirQuality(val aqi: AQI)

    data class AQI(val chn: Float)
}


// 获取实时天气信息接口所返回的JSON数据格式
/*
{
    "status": "ok",
    "result": {
        "realtime": {
            "temperature": 23.16,
            "skycon": "WIND",
            "air_quality": {
                "aqi": { "chn": 17.0 }
            }
        }
    }
}
*/
