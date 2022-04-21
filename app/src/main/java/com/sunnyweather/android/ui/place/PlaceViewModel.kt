package com.sunnyweather.android.ui.place

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.sunnyweather.android.logic.Repository
import com.sunnyweather.android.logic.model.Place

class PlaceViewModel : ViewModel() {
    // 定义一个新的searchLiveData对象，用来观察query的数据变化
    private val searchLiveData = MutableLiveData<String>()

    // 定义了一个placeList集合，用于对界面上显示的城市数据进行缓存
    // 因为原则上与界面相关的数据都应该放到ViewModel中，这样可以保证它们在手机屏幕发生旋转的时候不会丢失，编写UI层代码的时候会用到这个集合
    val placeList = ArrayList<Place>()

    // switchMap()方法同样接收两个参数：第一个参数传入我们新增的userIdLiveData，switchMap()方法会对它进行观察
    // 第二个参数是一个转换函数，注意，我们必须在这个转换函数中返回一个LiveData对象
    // switchMap()方法的工作原理就是要将转换函数中返回的LiveData对象转换成另一个可观察的LiveData对象
    val placeLiveData = Transformations.switchMap(searchLiveData) { query ->
        // 在转换函数中，我们只需要调用仓库层中定义的searchPlaces()方法就可以发起网络请求，同时将仓库层返回的LiveData对象转换成一个可供Activity观察的LiveData对象
        Repository.searchPlaces(query)
    }

    // 定义了一个searchPlaces()方法，但是这里并没有直接调用仓库层中的searchPlaces()方法，而是将传入的搜索参数赋值给了一个searchLiveData对象
    // 调用Transformations的switchMap()方法来观察这个对象，否则仓库层返回的LiveData对象将无法进行观察
    // 在每当searchPlaces()函数被调用时，switchMap()方法所对应的转换函数就会执行
    fun searchPlaces(query: String) {
        searchLiveData.value = query
    }

    fun savePlace(place: Place) = Repository.savePlace(place)

    fun getSavedPlace() = Repository.getSavedPlace()

    // 用于判断是否有数据已被存储
    fun isPlaceSaved() = Repository.isPlaceSaved()

}

/*
当外部调用PlaceViewModel的searchPlaces()方法来获取城市数据时，并不会发起任何请求或者函数调用，只会将传入的query值设置到searchLiveData当中
一旦searchLiveData的数据发生变化，那么观察searchLiveData的switchMap()方法就会执行，并且调用我们编写的转换函数
然后在转换函数中调用Repository.searchPlaces()方法获取真正的城市数据
同时，switchMap()方法会将Repository.searchPlaces()方法返回的LiveData对象转换成一个可观察的LiveData对象，对于Activity而言，只要去观察这个LiveData对象就可以了
*/
