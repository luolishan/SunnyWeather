package com.sunnyweather.android

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

class SunnyWeatherApplication : Application() {

    // 给项目提供一种全局获取Context的方式

    // 将 Context 设置为静态变量会产生内存泄漏问题
    // Android会给出警告，但是这里获取的不是 Activity 或 Service 中的 Context，而是 Application 中的 Context，
    // 它全局只会存在一份实例，并且在整个应用程序的生命周期内都不会回收，因此是不在在内存泄漏风险的。这里用 @SuppressLint 来忽略上述警告
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context

        // 全局上下文
        const val TOKEN = "dpjIFqmgELbN5KW1"               // 全局常量示例  填入你申请到的令牌值
    }

    override fun onCreate() {
        super.onCreate()
        // 调用getApplicationContext()方法得到的返回值赋值给context变量，这样我们就可以以静态变量的形式获取Context对象了
        context = applicationContext
    }

}