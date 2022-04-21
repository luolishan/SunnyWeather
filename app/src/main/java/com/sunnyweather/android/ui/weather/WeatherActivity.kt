package com.sunnyweather.android.ui.weather

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.*
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.sunnyweather.android.R
import com.sunnyweather.android.databinding.ActivityWeatherBinding
import com.sunnyweather.android.databinding.ForecastItemBinding
import com.sunnyweather.android.logic.model.Weather
import com.sunnyweather.android.logic.model.getSky
import java.text.SimpleDateFormat
import java.util.*

class WeatherActivity : AppCompatActivity() {
    lateinit var binding: ActivityWeatherBinding

    // 通过ViewModelProvider来获取ViewModel的实例
    // lazy函数这种懒加载技术来获取WeatherViewModel的实例，允许我们在整个类中随时使用viewModel这个变量，而完全不用关心它何时初始化、是否为空等前提条件
    val viewModel by lazy { ViewModelProvider(this).get(WeatherViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWeatherBinding.inflate(layoutInflater)

        // 调用了getWindow().getDecorView()方法拿到当前Activity的DecorView
        // 这里传入View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN和View.SYSTEM_UI_FLAG_LAYOUT_STABLE就表示Activity的布局会显示在状态栏上面
        val decorView = window.decorView
        decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE

        /*WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = ViewCompat.getWindowInsetsController(binding.root)
        controller?.hide(WindowInsetsCompat.Type.systemBars())
        controller?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE*/

        // 调用一下setStatusBarColor()方法将状态栏设置成透明色
        window.statusBarColor = Color.TRANSPARENT

        setContentView(binding.root)
        // 首先判断经纬度坐标和地区名称是否为空，空的话从Intent中取出经纬度坐标和地区名称，并赋值到WeatherViewModel的相应变量中
        if (viewModel.locationLng.isEmpty()) {
            viewModel.locationLng = intent.getStringExtra("location_lng") ?: ""
        }
        if (viewModel.locationLat.isEmpty()) {
            viewModel.locationLat = intent.getStringExtra("location_lat") ?: ""
        }
        if (viewModel.placeName.isEmpty()) {
            viewModel.placeName = intent.getStringExtra("place_name") ?: ""
        }
        // 对WeatherViewModel中的weatherLiveData对象进行观察，当获取到服务器返回的天气数据时，就会回调到传入的Observer接口实现中
        // 等数据获取完成之后，可观察LiveData对象的observe()方法将会得到通知，就调用showWeatherInfo()方法进行解析与展示
        viewModel.weatherLiveData.observe(this, Observer { result ->
            val weather = result.getOrNull()
            if (weather != null) {
                showWeatherInfo(weather)
            } else {
                Toast.makeText(this, "无法成功获取天气信息", Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }
            // 调用SwipeRefreshLayout的setRefreshing()方法并传入false，表示刷新事件结束，并隐藏刷新进度条
            binding.swipeRefresh.isRefreshing = false
        })
        // 下拉刷新功能
        // 调用SwipeRefreshLayout的setColorSchemeResources()方法来设置下拉刷新进度条的颜色
        binding.swipeRefresh.setColorSchemeResources(R.color.purple_500)
        // 调用一个refreshWeather()方法来执行一次刷新天气的请求
        refreshWeather()
        // 调用setOnRefreshListener()方法来设置一个下拉刷新的监听器，当用户进行了下拉刷新操作时，就会回调到Lambda表达式当中，然后处理具体的刷新逻辑就可以了
        binding.swipeRefresh.setOnRefreshListener {
            // 执行一次刷新天气的请求
            refreshWeather()
        }

        // 滑动菜单的逻辑处理
        // 切换城市的按钮点击事件
        binding.includeNow.navBtn.setOnClickListener {
            // 调用DrawerLayout的openDrawer()方法来打开滑动菜单
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
        // 监听DrawerLayout的状态，当滑动菜单被隐藏的时候，同时也要隐藏输入法
        binding.drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerStateChanged(newState: Int) {}
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
            override fun onDrawerOpened(drawerView: View) {}
            override fun onDrawerClosed(drawerView: View) {
                val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                manager.hideSoftInputFromWindow(drawerView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            }
        })
    }

    // 用于刷新天气信息
    fun refreshWeather() {
        // 调用了WeatherViewModel的refreshWeather()方法来执行一次刷新天气的请求
        viewModel.refreshWeather(viewModel.locationLng, viewModel.locationLat)
        // 让下拉刷新进度条显示出来
        binding.swipeRefresh.isRefreshing = true
    }

    private fun showWeatherInfo(weather: Weather) {
        binding.includeNow.placeName.text = viewModel.placeName
        val realtime = weather.realtime
        val daily = weather.daily

        // 填充now.xml布局中的数据 当前天气信息
        val currentTempText = "${realtime.temperature.toInt()} ℃"
        binding.includeNow.currentTemp.text = currentTempText
        binding.includeNow.currentSky.text = getSky(realtime.skycon).info
        val currentPM25Text = "空气指数 ${realtime.airQuality.aqi.chn.toInt()}"
        binding.includeNow.currentAQI.text = currentPM25Text
        // 设置当前天气信息布局的背景图片
        binding.includeNow.nowLayout.setBackgroundResource(getSky(realtime.skycon).bg)

        // 填充forecast.xml布局中的数据 未来几天天气信息
        binding.includeForecast.forecastLayout.removeAllViews()
        // 获取有几天的天气信息
        val days = daily.skycon.size
        // 使用了一个for-in循环来处理每天的天气信息
        for (i in 0 until days) {
            val skycon = daily.skycon[i]
            val temperature = daily.temperature[i]
            // 在循环中动态加载forecast_item.xml布局并设置相应的数据，然后添加到父布局中
//            val view = LayoutInflater.from(this).inflate(R.layout.forecast_item, binding.includeForecast.forecastLayout, false)
            val forecastItemBinding = ForecastItemBinding.inflate(LayoutInflater.from(this), binding.includeForecast.forecastLayout, false)
            /*val dateInfo = view.findViewById(R.id.dateInfo) as TextView
            val skyIcon = view.findViewById(R.id.skyIcon) as ImageView
            val skyInfo = view.findViewById(R.id.skyInfo) as TextView
            val temperatureInfo = view.findViewById(R.id.temperatureInfo) as TextView*/

            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            forecastItemBinding.dateInfo.text = simpleDateFormat.format(skycon.date)
            val sky = getSky(skycon.value)
            forecastItemBinding.skyIcon.setImageResource(sky.icon)
            forecastItemBinding.skyInfo.text = sky.info
            val tempText = "${temperature.min.toInt()} ~ ${temperature.max.toInt()} ℃"
            forecastItemBinding.temperatureInfo.text = tempText
            binding.includeForecast.forecastLayout.addView(forecastItemBinding.root)
        }
        // 填充life_index.xml布局中的数据
        // 生活指数方面虽然服务器会返回很多天的数据，但是界面上只需要当天的数据就可以了，因此这里我们对所有的生活指数都取了下标为零的那个元素的数据
        val lifeIndex = daily.lifeIndex
        binding.includeLifeIndex.coldRiskText.text = lifeIndex.coldRisk[0].desc
        binding.includeLifeIndex.dressingText.text = lifeIndex.dressing[0].desc
        binding.includeLifeIndex.ultravioletText.text = lifeIndex.ultraviolet[0].desc
        binding.includeLifeIndex.carWashingText.text = lifeIndex.carWashing[0].desc
        // 让ScrollView变成可见状态
        binding.weatherLayout.visibility = View.VISIBLE
    }
}