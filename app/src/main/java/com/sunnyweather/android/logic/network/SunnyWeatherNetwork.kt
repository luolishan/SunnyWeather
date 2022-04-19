package com.sunnyweather.android.logic.network

import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/*当外部调用SunnyWeatherNetwork的searchPlaces()函数时，Retrofit就会立即发起网络请求，同时当前的协程也会被阻塞住
直到服务器响应我们的请求之后，await()函数会将解析出来的数据模型对象取出并返回，同时恢复当前协程的执行，searchPlaces()函
数在得到await()函数的返回值后会将该数据再返回到上一层*/
object SunnyWeatherNetwork {

    // 调用Retrofit对象的create()方法，并传入具体Service接口所对应的Class类型，创建了一个PlaceService接口的动态代理对象
    private val placeService = ServiceCreator.create<PlaceService>()

    // 定义了一个searchPlaces()挂起函数，调用刚刚在PlaceService接口中定义的searchPlaces()方法，以发起搜索城市数据请求
    suspend fun searchPlaces(query: String) = placeService.searchPlaces(query).await()

    // 借助协程技术来简化Retrofit回调的写法
    // suspend关键字只能将一个函数声明成挂起函数，是无法给它提供协程作用域的
    // await()函数是一个挂起函数，然后我们给它声明了一个泛型T，并将await()函数定义成了Call<T>的扩展函数，这样所有返回值是Call类型的Retrofit网络请求接口就都可以直接调用await()函数了
    private suspend fun <T> Call<T>.await(): T {
        // 使用了suspendCoroutine函数来挂起当前协程
        // suspendCoroutine函数必须在协程作用域或挂起函数中才能调用，它接收一个Lambda表达式参数，主要作用是将当前协程立即挂起
        // 然后在一个普通的线程中执行Lambda表达式中的代码。Lambda表达式的参数列表上会传入一个Continuation参数，调用它的resume()方法或resumeWithException()可以让协程恢复执行
        return suspendCoroutine { continuation ->
            // 并且由于扩展函数的原因，我们现在拥有了Call对象的上下文，那么这里就可以直接调用enqueue()方法让Retrofit发起网络请求
            // 接下来，使用同样的方式对Retrofit响应的数据或者网络请求失败的情况进行处理就可以了
            enqueue(object : Callback<T> {
                // 在onResponse()回调当中，我们调用body()方法解析出来的对象是可能为空的
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    val body = response.body()
                    if (body != null) continuation.resume(body)
                    // 如果为空的话，这里的做法是手动抛出一个异常，你也可以根据自己的逻辑进行更加合适的处理
                    else continuation.resumeWithException(
                        RuntimeException("response body is null"))
                }
                override fun onFailure(call: Call<T>, t: Throwable) {
                    continuation.resumeWithException(t)
                }
            })
        }
    }
}