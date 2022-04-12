package com.delivery.sopo.models.base

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.enums.ErrorEnum
import com.delivery.sopo.enums.NetworkStatus
import com.delivery.sopo.exceptions.InternalServerException
import com.delivery.sopo.exceptions.OAuthException
import com.delivery.sopo.exceptions.SOPOApiException
import com.delivery.sopo.interfaces.listener.OnSOPOErrorCallback
import com.delivery.sopo.usecase.LogoutUseCase
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.ui_util.OnSnackBarClickListener
import kotlinx.coroutines.*
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*

abstract class BaseViewModel: ViewModel(), KoinComponent
{
    private val logoutUseCase: LogoutUseCase by inject()

//    abstract val exceptionHandler: CoroutineExceptionHandler

    protected val scope: CoroutineScope = (viewModelScope + SupervisorJob())

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

    open lateinit var onSOPOErrorCallback: OnSOPOErrorCallback

    protected val coroutineExceptionHandler: CoroutineExceptionHandler =
        CoroutineExceptionHandler { _, exception ->

            when(exception)
            {
                is SOPOApiException ->
                {
                    val errorCode = ErrorEnum.getErrorCode(exception.getErrorResponse().code).apply {
                        message = exception.getErrorResponse().message
                    }

                    SopoLog.e("SOPO API Error $errorCode", exception)

                    when(errorCode)
                    {
                        ErrorEnum.ALREADY_REGISTERED_PARCEL, ErrorEnum.OVER_REGISTERED_PARCEL, ErrorEnum.PARCEL_BAD_REQUEST -> onSOPOErrorCallback.onRegisterParcelError(errorCode)
                        ErrorEnum.ALREADY_REGISTERED_USER -> onSOPOErrorCallback.onAlreadyRegisteredUser(errorCode)
                        ErrorEnum.PARCEL_NOT_FOUND -> onSOPOErrorCallback.onInquiryParcelError(errorCode)
                        else -> onSOPOErrorCallback.onFailure(errorCode)
                    }
                }
                is OAuthException ->
                {
                    val errorCode = ErrorEnum.getErrorCode(exception.getErrorResponse().code).apply {
                        message = exception.getErrorResponse().message
                    }

                    SopoLog.e("OAuthException API Error $errorCode", exception)

                    when(errorCode)
                    {
                        ErrorEnum.OAUTH2_INVALID_GRANT, ErrorEnum.OAUTH2_INVALID_TOKEN ->
                        {
                            onSOPOErrorCallback.onLoginError(errorCode)
                        }
                        ErrorEnum.OAUTH2_DELETE_TOKEN ->
                        {
                            logoutUseCase.invoke()
                            moveDuplicated()
                        }
                        else -> onSOPOErrorCallback.onAuthError(errorCode)
                    }
                }
                is InternalServerException ->
                {
                    val errorCode = ErrorEnum.getErrorCode(exception.getErrorResponse().code).apply {
                            message = exception.getErrorResponse().message
                        }
                    SopoLog.e("InternalServerException API Error $errorCode", exception)

                    if(errorCode == ErrorEnum.FAIL_TO_SEARCH_PARCEL)
                    {
                        return@CoroutineExceptionHandler onSOPOErrorCallback.onInquiryParcelError(errorCode)
                    }

                    onSOPOErrorCallback.onInternalServerError(errorCode)
                }
                else ->
                {
                    onSOPOErrorCallback.onFailure(ErrorEnum.UNKNOWN_ERROR)
                }
            }
        }

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

        if(_isClickEvent.value == true) return

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

    fun moveDuplicated()
    {
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
        _isClickEvent.value = false
        scope.cancel()
    }
}