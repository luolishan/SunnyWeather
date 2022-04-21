package com.sunnyweather.android.ui.place

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.sunnyweather.android.R
import com.sunnyweather.android.databinding.PlaceItemBinding
import com.sunnyweather.android.logic.model.Place
import com.sunnyweather.android.ui.weather.WeatherActivity

// 把PlaceAdapter主构造函数中传入的Fragment对象改成PlaceFragment对象，这样我们就可以调用PlaceFragment所对应的PlaceViewModel了
class PlaceAdapter(private val fragment: PlaceFragment, private val placeList: List<Place>) :
    RecyclerView.Adapter<PlaceAdapter.ViewHolder>() {

    inner class ViewHolder(binding: PlaceItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val placeName: TextView = binding.placeName
        val placeAddress: TextView = binding.placeAddress
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = PlaceItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        // 添加点击事件
        val holder = ViewHolder(binding)
        // itemView表示最外层布局
        // 给place_item.xml的最外层布局注册了一个点击事件监听器
        holder.itemView.setOnClickListener {
            // 获取用户点击的position
            val position = holder.bindingAdapterPosition
            // 通过position拿到相应的place实例
            // 获取当前点击项的城市名称和经纬度坐标
            val place = placeList[position]
            val activity = fragment.activity
            if (activity is WeatherActivity) {
                // 关闭滑动菜单
                activity.binding.drawerLayout.closeDrawers()
                // 给WeatherViewModel赋值新的经纬度坐标和地区名称
                activity.viewModel.locationLng = place.location.lng
                activity.viewModel.locationLat = place.location.lat
                activity.viewModel.placeName = place.name
                // 刷新城市的天气信息
                activity.refreshWeather()
            } else {
                // 把place实例传入Intent中，最后调用startActivity()方法启动WeatherActivity
                val intent = Intent(parent.context, WeatherActivity::class.java).apply {
                    putExtra("location_lng", place.location.lng)
                    putExtra("location_lat", place.location.lat)
                    putExtra("place_name", place.name)
                }
                // 调用Fragment的startActivity()方法启动WeatherActivity
                fragment.startActivity(intent)
                activity?.finish()
            }
            // 当点击了任何子项布局时，在跳转到WeatherActivity之前，先调用PlaceViewModel的savePlace()方法来存储选中的城市
            fragment.viewModel.savePlace(place)
        }
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val place = placeList[position]
        holder.placeName.text = place.name
        holder.placeAddress.text = place.address
    }

    override fun getItemCount() = placeList.size
}