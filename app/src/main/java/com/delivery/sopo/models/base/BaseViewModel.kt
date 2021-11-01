package com.delivery.sopo.models.base

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.enums.NetworkStatus
import com.delivery.sopo.util.SopoLog
import io.reactivex.Flowable.just
import io.reactivex.Observable
import io.reactivex.Observable.just
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import java.util.*

abstract class BaseViewModel:ViewModel()
{
    private val _isCheckNetwork = MutableLiveData<Boolean>()
    val isCheckNetwork: LiveData<Boolean>
        get() = _isCheckNetwork

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    private val _errorSnackBar = MutableLiveData<String>()
    val errorSnackBar: LiveData<String>
        get() = _errorSnackBar

    val scope: CoroutineScope = viewModelScope

    abstract val exceptionHandler: CoroutineExceptionHandler

    fun checkNetworkStatus():Boolean{

        val networkStatus = getConnectivityStatus(SOPOApp.INSTANCE)

        if(networkStatus != NetworkStatus.NOT_CONNECT)
        {
            stopToCheckNetworkStatus()
            return true
        }

        startToCheckNetworkStatus()

        SOPOApp.networkStatus.postValue(networkStatus)

        return false
    }

    fun startToCheckNetworkStatus(){
        _isCheckNetwork.postValue(true)
    }

    fun stopToCheckNetworkStatus(){
        _isCheckNetwork.postValue(false)
    }

    fun postErrorSnackBar(msg: String){
        SopoLog.d("MSG : $msg")
        _errorSnackBar.postValue(msg)
    }

    fun onStartLoading(){
        _isLoading.postValue(true)
    }

    fun onStopLoading(){
        _isLoading.postValue(false)
    }

    override fun onCleared()
    {
        super.onCleared()
    }

    fun getConnectivityStatus(context: Context): NetworkStatus
    {
        // 네트워크 연결 상태 확인하기 위한 ConnectivityManager 객체 생성
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            // 활성화된 네트워크의 상태를 표현하는 객체
            val nc = cm.getNetworkCapabilities(cm.activeNetwork) ?: return NetworkStatus.NOT_CONNECT

            return when
            {
                nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ->
                {
                    NetworkStatus.WIFI
                }
                nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ->
                {
                    NetworkStatus.CELLULAR
                }
                else -> NetworkStatus.NOT_CONNECT
            }
        }

        // 기기 버전이 마시멜로우 버전보다 아래인 경우
        // getActiveNetworkInfo -> API level 29에 디플리케이트 됨
        val activeNetwork = cm.activeNetworkInfo ?: return NetworkStatus.NOT_CONNECT

        return when(activeNetwork.type)
        {
            ConnectivityManager.TYPE_WIFI ->
            {
                NetworkStatus.WIFI
            }
            ConnectivityManager.TYPE_MOBILE ->
            {
                NetworkStatus.CELLULAR
            }
            else -> NetworkStatus.NOT_CONNECT
        }
    }
}