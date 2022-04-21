package com.sunnyweather.android.logic

import androidx.lifecycle.liveData
import com.sunnyweather.android.logic.dao.PlaceDao
import com.sunnyweather.android.logic.model.Place
import com.sunnyweather.android.logic.model.Weather
import com.sunnyweather.android.logic.network.SunnyWeatherNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlin.coroutines.CoroutineContext

object Repository {

    // liveData()函数是lifecycle-livedata-ktx库提供的一个非常强大且好用的功能，它可以自动构建并返回一个LiveData对象
    // 然后在它的代码块中提供一个挂起函数的上下文，这样我们就可以在liveData()函数的代码块中调用任意的挂起函数了
    // 将liveData()函数的线程参数类型指定成了Dispatchers.IO，这样代码块中的所有代码就都运行在子线程中了
    // Android是不允许在主线程中进行网络请求的，诸如读写数据库之类的本地数据操作也是不建议在主线程中进行的，因此非常有必要在仓库层进行一次线程转换
    fun searchPlaces(query: String) = fire(Dispatchers.IO) {
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
    }

    // 用来刷新天气信息
    fun refreshWeather(lng: String, lat: String) = fire(Dispatchers.IO) {
        // 由于async函数必须在协程作用域内才能调用，所以这里又使用coroutineScope函数创建了一个协程作用域
        coroutineScope {
            // 获取实时天气信息和获取未来天气信息这两个请求是没有先后顺序的，因此让它们并发执行可以提升程序的运行效率
            // 分别在两个async函数中发起网络请求，然后再分别调用它们的await()方法，就可以保证只有在两个网络请求都成功响应之后，才会进一步执行程序
            val deferredRealtime = async {
                SunnyWeatherNetwork.getRealtimeWeather(lng, lat)
            }
            val deferredDaily = async {
                SunnyWeatherNetwork.getDailyWeather(lng, lat)
            }
            // 需要获取async函数代码块的执行结果，只需要调用Deferred对象的await()方法
            // 当调用await()方法时，如果async代码块中的代码还没有执行完，那么await()方法会将当前的协程阻塞住，直到可以获取async函数的执行结果
            val realtimeResponse = deferredRealtime.await()
            val dailyResponse = deferredDaily.await()
            if (realtimeResponse.status == "ok" && dailyResponse.status == "ok") {
                // 将Realtime和Daily对象取出并封装到一个Weather对象中，然后使用Result.success()方法来包装这个Weather对象
                val weather = Weather(
                    realtimeResponse.result.realtime,
                    dailyResponse.result.daily
                )
                Result.success(weather)
            } else {
                // 否则就使用Result.failure()方法来包装一个异常信息
                Result.failure(
                    RuntimeException(
                        "realtime response status is ${realtimeResponse.status}" +
                                "daily response status is ${dailyResponse.status}"
                    )
                )
            }
        }
    }

    // 可以在某个统一的入口函数中进行封装，使得只要进行一次try catch处理就行了
    // 在liveData()函数的代码块中，我们是拥有挂起函数上下文的，可是当回调到Lambda表达式中，代码就没有挂起函数上下文了，但实际上Lambda表达式中的代码一定也是在挂起函数中运行的
    // 为了解决这个问题，我们需要在函数类型前声明一个suspend关键字，以表示所有传入的Lambda表达式中的代码也是拥有挂起函数上下文的
    private fun <T> fire(context: CoroutineContext, block: suspend () -> Result<T>) =
        liveData<Result<T>>(context) {
            val result = try {
                block()
            } catch (e: Exception) {
                Result.failure<T>(e)
            }
            // 使用emit()方法将包装的结果发射出去，这个emit()方法其实类似于调用LiveData的setValue()方法来通知数据变化
            // 只不过这里我们无法直接取得返回的LiveData对象，所以lifecycle-livedata-ktx库提供了这样一个替代方法
            emit(result)
        }


    fun savePlace(place: Place) = PlaceDao.savePlace(place)

    fun getSavedPlace() = PlaceDao.getSavedPlace()

    // 用于判断是否有数据已被存储
    fun isPlaceSaved() = PlaceDao.isPlaceSaved()

}