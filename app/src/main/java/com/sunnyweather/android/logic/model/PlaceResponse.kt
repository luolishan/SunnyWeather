package com.sunnyweather.android.logic.model

import com.google.gson.annotations.SerializedName

data class PlaceResponse(val status: String, val places: List<Place>)

data class Place(
    val name: String, val location: Location,
    @SerializedName("formatted_address") val address: String
)

/* 由于JSON中一些字段的命名可能与Kotlin的命名规范不太一致，
因此这里使用了@SerializedName注解的方式，来让JSON字段和Kotlin字段之间建立映射关系 */

data class Location(val lng: String, val lat: String)


// 搜索城市数据接口返回的JSON格式
/*
{"status":"ok","query":"北京",
    "places":[
    {"name":"北京市","location":{"lat":39.9041999,"lng":116.4073963},
        "formatted_address":"中国北京市"},
    {"name":"北京西站","location":{"lat":39.89491,"lng":116.322056},
        "formatted_address":"中国 北京市 丰台区 莲花池东路118号"},
    {"name":"北京南站","location":{"lat":39.865195,"lng":116.378545},
        "formatted_address":"中国 北京市 丰台区 永外大街车站路12号"},
    {"name":"北京站(地铁站)","location":{"lat":39.904983,"lng":116.427287},
        "formatted_address":"中国 北京市 东城区 2号线"}
    ]}
*/
