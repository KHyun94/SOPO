package com.delivery.sopo.models.base

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.enums.NetworkStatus
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.ui_util.OnSnackBarClickListener
import io.reactivex.Flowable.just
import io.reactivex.Observable
import io.reactivex.Observable.just
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import java.util.*

abstract class BaseViewModel: ViewModel()
{
    abstract val exceptionHandler: CoroutineExceptionHandler

    val scope: CoroutineScope = (viewModelScope + Job())

    var onSnackClickListener: Pair<CharSequence, OnSnackBarClickListener>? = null

    private val _isClickEvent = MutableLiveData<Boolean>()
    val isClickEvent: LiveData<Boolean>
        get() = _isClickEvent

    private val _isCheckNetwork = MutableLiveData<Boolean>()
    val isCheckNetwork: LiveData<Boolean>
        get() = _isCheckNetwork

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    private val _errorSnackBar = MutableLiveData<String>()
    val errorSnackBar: LiveData<String>
        get() = _errorSnackBar

    private val _isDuplicated = MutableLiveData<Boolean>()
    val isDuplicated: LiveData<Boolean>
        get() = _isDuplicated

    fun checkEventStatus(checkNetwork: Boolean = false, delayMillisecond: Long = 100, event: () -> Unit)
    {
        if(_isClickEvent.value == true) return

        _isClickEvent.postValue(true)

        if(checkNetwork)
        {
            checkNetworkStatus().also { value ->
                if(!value)
                {
                    SopoLog.e("인터넷 오류")
                    _isClickEvent.postValue(false)
                    return
                }
            }
        }
        else
        {
            _isClickEvent.postValue(false)
        }

        SopoLog.d("인터넷 체크 후")

        //        if(_isClickEvent.value == true) return

        SopoLog.d("이벤트 시작 전")

        Handler(Looper.getMainLooper()).postDelayed({
            try
            {
                SopoLog.d("이벤트 시작")
                event.invoke()
            }
            finally
            {
                SopoLog.d("이벤트 종료")
                _isClickEvent.postValue(false)
            }
        }, delayMillisecond)
    }



    fun checkNetworkStatus(): Boolean
    {
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

    fun startLoading()
    {
        SopoLog.d("start isLoading ${_isLoading.value}")
        if(_isLoading.value == true) return
        _isLoading.postValue(true)
    }

    fun stopLoading()
    {
        SopoLog.d("stop isLoading ${_isLoading.value}")
        if(_isLoading.value == false) return
        _isLoading.postValue(false)
    }


    fun startToCheckNetworkStatus()
    {
        _isCheckNetwork.postValue(true)
    }

    fun stopToCheckNetworkStatus()
    {
        _isCheckNetwork.postValue(false)
    }

    fun postErrorSnackBar(msg: String, onSnackBarClickListener: Pair<CharSequence, OnSnackBarClickListener>? = null)
    {
        SopoLog.d("MSG : $msg")
        _errorSnackBar.postValue(msg)
        this.onSnackClickListener = onSnackBarClickListener
    }

    fun onStartLoading()
    {
        _isLoading.postValue(true)
    }

    fun onStopLoading()
    {
        _isLoading.postValue(false)
    }

    fun moveDuplicated(){
        _isDuplicated.postValue(true)
        scope.cancel("중복 로그인으로 인한 모든 프로세스 종료")
    }

    fun getConnectivityStatus(context: Context): NetworkStatus
    { // 네트워크 연결 상태 확인하기 위한 ConnectivityManager 객체 생성
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        { // 활성화된 네트워크의 상태를 표현하는 객체
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

    override fun onCleared()
    {
        super.onCleared()

        scope.cancel()
    }
}