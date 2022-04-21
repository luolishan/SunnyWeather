package com.sunnyweather.android.ui.weather

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.sunnyweather.android.logic.Repository
import com.sunnyweather.android.logic.model.Location

class WeatherViewModel : ViewModel() {
    // 定义一个新的locationLiveData对象，用来观察Location的数据变化
    private val locationLiveData = MutableLiveData<Location>()

    // 定义了locationLng、locationLat和placeName这3个变量，它们都是和界面相关的数据
    // 放到ViewModel中可以保证它们在手机屏幕发生旋转的时候不会丢失，编写UI层代码的时候会用到这几个变量
    var locationLng = ""
    var locationLat = ""
    var placeName = ""

    // 然后使用Transformations的switchMap()方法来观察这个对象，并在switchMap()方法的转换函数中调用仓库层中定义的refreshWeather()方法
    // 这样，仓库层返回的LiveData对象就可以转换成一个可供Activity观察的LiveData对象了
    val weatherLiveData = Transformations.switchMap(locationLiveData) { location ->
        // 在转换函数中，我们只需要调用仓库层中定义的refreshWeather()方法就可以发起网络请求，同时将仓库层返回的LiveData对象转换成一个可供Activity观察的LiveData对象
        Repository.refreshWeather(location.lng, location.lat)
    }

    // 刷新天气信息，并将传入的经纬度参数封装成一个Location对象后赋值给locationLiveData对象
    fun refreshWeather(lng: String, lat: String) {
        locationLiveData.value = Location(lng, lat)
    }
}