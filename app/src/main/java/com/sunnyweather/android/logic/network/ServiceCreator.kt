package com.sunnyweather.android.logic.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Retrofit构建器
object ServiceCreator {
    // 在它的内部定义了一个BASE_URL常量，用于指定Retrofit的根路径
    private const val BASE_URL = "https://api.caiyunapp.com/"
    // 使用了Retrofit.Builder来构建一个Retrofit对象
    private val retrofit = Retrofit.Builder()
        // baseUrl()方法用于指定所有Retrofit请求的根路径(必须调用)
        .baseUrl(BASE_URL)
        // addConverterFactory()方法用于指定Retrofit在解析数据时所使用的转换库，这里指定成GsonConverterFactory(必须调用)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // 提供了一个外部可见的create()方法，并接收一个Class类型的参数
    // 当在外部调用这个方法时，实际上就是调用了Retrofit对象的create()方法，从而创建出相应Service接口的动态代理对象
    fun <T> create(serviceClass: Class<T>): T = retrofit.create(serviceClass)

    // 定义了一个不带参数的create()方法，并使用inline关键字来修饰方法，使用reified关键字来修饰泛型，这是泛型实化的两大前提条件
    // 接下来就可以使用T::class.java这种语法了，这里调用刚才定义的带有Class参数的create()方法即可
    inline fun <reified T> create(): T = create(T::class.java)

}