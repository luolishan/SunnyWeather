package com.sunnyweather.android.ui.place

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.sunnyweather.android.MainActivity
import com.sunnyweather.android.R
import com.sunnyweather.android.databinding.FragmentPlaceBinding
import com.sunnyweather.android.ui.weather.WeatherActivity

class PlaceFragment : Fragment() {
    private var _binding: FragmentPlaceBinding? = null
    private val binding get() = _binding!!

    // 通过ViewModelProvider来获取ViewModel的实例
    // lazy函数这种懒加载技术来获取PlaceViewModel的实例，允许我们在整个类中随时使用viewModel这个变量，而完全不用关心它何时初始化、是否为空等前提条件
    val viewModel by lazy { ViewModelProvider(this).get(PlaceViewModel::class.java) }
    private lateinit var adapter: PlaceAdapter

    // onCreateView()方法中加载了前面编写的fragment_place布局
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPlaceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // 只有当PlaceFragment被嵌入MainActivity中，并且当前已有存储的城市数据，那么就获取已存储的数据并解析成Place对象，然后使用它的经纬度坐标和城市名直接跳转并传递给WeatherActivity
        // 这样用户就不需要每次都重新搜索并选择城市了
        // 用于判断是否有数据已被存储
        if (activity is MainActivity && viewModel.isPlaceSaved()) {
            // 读取数据的接口-将Place对象读取出来
            // 获取当前点击项的城市名称和经纬度坐标
            val place = viewModel.getSavedPlace()
            // 把place实例传入Intent中，最后调用startActivity()方法启动WeatherActivity
            val intent = Intent(context, WeatherActivity::class.java).apply {
                putExtra("location_lng", place.location.lng)
                putExtra("location_lat", place.location.lat)
                putExtra("place_name", place.name)
            }
            // 调用Fragment的startActivity()方法启动WeatherActivity
            startActivity(intent)
            activity?.finish()
            return
        }
        // LinearLayoutManager 实现纵向滚动
        // LayoutManager用于指定RecyclerView的布局方式
        // LinearLayoutManager是线性布局
        // 创建LinearLayoutManager对象
        val layoutManager = LinearLayoutManager(activity)
        // 把LinearLayoutManager对象设置到RecyclerView当中,指定RecyclerView的布局方式
        // recyclerView 为RecyclerView的id
        binding.recyclerView.layoutManager = layoutManager

        // 创建PlaceAdapter实例（适配器）
        // viewModel.placeList 获取界面上显示的城市缓存的数据（手机屏幕发生旋转的时候不会丢失）
        // 使用PlaceViewModel中的placeList集合作为数据源
        adapter = PlaceAdapter(this, viewModel.placeList)
        // 调用RecyclerView的setAdapter()方法，将构建好的适配器对象传递进去，这样RecyclerView和数据之间的关联就建立好了
        binding.recyclerView.adapter = adapter

        // 调用了EditText的addTextChangedListener()方法来监听搜索框内容的变化情况
        binding.searchPlaceEdit.addTextChangedListener { editable ->
            val content = editable.toString()
            if (content.isNotEmpty()) {
                // 每当搜索框中的内容发生了变化，我们就获取新的内容，然后传递给PlaceViewModel的searchPlaces()方法，这样就可以发起搜索城市数据的网络请求了
                viewModel.searchPlaces(content)
            } else {
                // 而当输入搜索框中的内容为空时，我们就将RecyclerView隐藏起来，同时将那张仅用于美观用途的背景图显示出来。
                binding.recyclerView.visibility = View.GONE
                binding.bgImageView.visibility = View.VISIBLE
                viewModel.placeList.clear()
                // 通知数据发生了变化
                adapter.notifyDataSetChanged()
            }
        }

        // 对PlaceViewModel中的placeLiveData对象进行观察，当有任何数据变化时，就会回调到传入的Observer接口实现中
        // 等数据获取完成之后，可观察LiveData对象的observe()方法将会得到通知，我们在这里将获取的数据显示到界面上
        viewModel.placeLiveData.observe(viewLifecycleOwner, Observer { result ->
            val places = result.getOrNull()
            if (places != null) {
                // 如果数据不为空，那么就将这些数据添加到PlaceViewModel的placeList集合中，并通知PlaceAdapter刷新界面；
                binding.recyclerView.visibility = View.VISIBLE
                binding.bgImageView.visibility = View.GONE
                viewModel.placeList.clear()
                viewModel.placeList.addAll(places)
                adapter.notifyDataSetChanged()
            } else {
                // 如果数据为空，则说明发生了异常，此时弹出一个Toast提示，并将具体的异常原因打印出来
                Toast.makeText(activity, "未能查询到任何地点", Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }
        })
    }
}