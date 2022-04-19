package com.sunnyweather.android.logic

import androidx.lifecycle.liveData
import com.sunnyweather.android.logic.model.Place
import com.sunnyweather.android.logic.network.SunnyWeatherNetwork
import kotlinx.coroutines.Dispatchers

object Repository {

    // liveData()函数是lifecycle-livedata-ktx库提供的一个非常强大且好用的功能，它可以自动构建并返回一个LiveData对象
    // 然后在它的代码块中提供一个挂起函数的上下文，这样我们就可以在liveData()函数的代码块中调用任意的挂起函数了
    // 将liveData()函数的线程参数类型指定成了Dispatchers.IO，这样代码块中的所有代码就都运行在子线程中了
    // Android是不允许在主线程中进行网络请求的，诸如读写数据库之类的本地数据操作也是不建议在主线程中进行的，因此非常有必要在仓库层进行一次线程转换
    fun searchPlaces(query: String) = liveData(Dispatchers.IO) {
        val result = try {
            // 这里调用了SunnyWeatherNetwork的searchPlaces()函数来搜索城市数据
            val placeResponse = SunnyWeatherNetwork.searchPlaces(query)
            // 然后判断如果服务器响应的状态是ok，那么
            if (placeResponse.status == "ok") {
                val places = placeResponse.places
                //就使用Kotlin内置的Result.success()方法来包装获取的城市数据列表
                Result.success(places)
            } else {
                // 否则使用Result.failure()方法来包装一个异常信息
                Result.failure(RuntimeException("response status is ${placeResponse.status}"))
            }
        } catch (e: Exception) {
            Result.failure<List<Place>>(e)
        }
        // 使用emit()方法将包装的结果发射出去，这个emit()方法其实类似于调用LiveData的setValue()方法来通知数据变化
        // 只不过这里我们无法直接取得返回的LiveData对象，所以lifecycle-livedata-ktx库提供了这样一个替代方法
        emit(result)
    }

}