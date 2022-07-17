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
import com.delivery.sopo.SOPOApplication
import com.delivery.sopo.enums.ErrorCode
import com.delivery.sopo.enums.NetworkStatus
import com.delivery.sopo.exceptions.InternalServerException
import com.delivery.sopo.exceptions.SOPOApiException
import com.delivery.sopo.interfaces.listener.OnSOPOErrorCallback
import com.delivery.sopo.domain.usecase.user.token.LogoutUseCase
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.ui_util.BottomNotificationBar
import com.delivery.sopo.util.ui_util.OnSnackBarClickListener
import kotlinx.coroutines.*
import org.koin.core.KoinComponent
import org.koin.core.inject

abstract class BaseViewModel: ViewModel(), KoinComponent
{
    var onSnackClickListener: Pair<CharSequence, OnSnackBarClickListener<Unit>>? = null

    private val _isClickEvent = MutableLiveData<Boolean>()
    val isClickEvent: LiveData<Boolean> = _isClickEvent

    private var isCheckNetwork: Boolean = false

    var currentNetworkState: NetworkStatus = NetworkStatus.DEFAULT

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _snackBar = MutableLiveData<BottomNotificationBar>()
    val snackBar: LiveData<BottomNotificationBar> = _snackBar

    private val _errorSnackBar = MutableLiveData<String>()
    val errorSnackBar: LiveData<String> = _errorSnackBar

    private val _toast = MutableLiveData<String>()
    val toast: LiveData<String> = _toast

    private val _isDuplicated = MutableLiveData<Boolean>()
    val isDuplicated: LiveData<Boolean> = _isDuplicated

    private val job = SupervisorJob()

    val exceptionHandler: CoroutineExceptionHandler =
        CoroutineExceptionHandler { coroutineContext, throwable ->
            when(throwable)
            {
                is SOPOApiException -> handlerAPIException(throwable)
                is InternalServerException -> postErrorSnackBar(throwable.message)
                else ->
                {
                    throwable.printStackTrace()
                    postErrorSnackBar(throwable.message ?: "확인할 수 없는 에러입니다.")
                }
            }
        }

    protected open fun handlerAPIException(exception: SOPOApiException){
        if(exception.code == ErrorCode.DUPLICATE_LOGIN)
        {
            _isDuplicated.postValue(true)
            scope.cancel("중복 로그인으로 인한 모든 프로세스 종료")
            return
        }
    }

    protected open fun handlerInternalServerException(exception: InternalServerException){

    }

    protected open fun handlerException(exception: Exception){

    }

    protected val scope: CoroutineScope = (viewModelScope + job + exceptionHandler)

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
        val networkStatus = getConnectivityStatus(SOPOApplication.INSTANCE)

        if(networkStatus != NetworkStatus.NOT_CONNECT)
        {
            setCheckNetwork(false)
            return true
        }

        setCheckNetwork(true)

        SOPOApplication.networkStatus.postValue(networkStatus)

        return false
    }

    fun isCheckNetwork(): Boolean
    {
        return this.isCheckNetwork
    }

    fun setCheckNetwork(isCheckNetwork: Boolean)
    {
        this.isCheckNetwork = isCheckNetwork
    }

    fun postSnackBar(bottomNotificationBar: BottomNotificationBar)
    {
        _snackBar.postValue(bottomNotificationBar)
    }

    fun postErrorSnackBar(msg: String, onSnackBarClickListener: Pair<CharSequence, OnSnackBarClickListener<Unit>>? = null)
    {
        _errorSnackBar.postValue(msg)
        this.onSnackClickListener = onSnackBarClickListener
    }

    fun postToast(msg: String)
    {
        _toast.postValue(msg)
    }

    fun onStartLoading()
    {
        _isLoading.postValue(true)
    }

    fun onStopLoading()
    {
        _isLoading.postValue(false)
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

    /*protected val coroutineExceptionHandler: CoroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->

            when(exception)
            {
                is SOPOApiException ->
                {
                    SopoLog.e("SOPO API Error ${exception.code}", exception)

                    when(val code = exception.code)
                    {
                        ErrorCode.VALIDATION -> _errorSnackBar.postValue(exception.message)
                        ErrorCode.ALREADY_REGISTERED_PARCEL, ErrorCode.OVER_REGISTERED_PARCEL, ErrorCode.PARCEL_BAD_REQUEST -> onSOPOErrorCallback.onRegisterParcelError(code)
                        ErrorCode.ALREADY_REGISTERED_USER -> onSOPOErrorCallback.onAlreadyRegisteredUser(code)
                        ErrorCode.PARCEL_NOT_FOUND -> onSOPOErrorCallback.onInquiryParcelError(code)
                        ErrorCode.AUTHENTICATION_FAIL ->
                        { // 서버에 유저 토큰이 없거나, 내부에 저장된 토큰이 없는 경우
                        }
                        ErrorCode.INVALID_JWT_TOKEN ->
                        { // Refresh Token이 잘못된 경우
                        }
                        ErrorCode.USER_NOT_FOUND ->
                        { // 존재하지 않은 계정
                        }
                        ErrorCode.INVALID_USER ->
                        { // 아이디 or 패스워드가 틀렸을 때 And 탈퇴한 회원이 요청했을 때 ?
                            onSOPOErrorCallback.onLoginError(code)
                        }
                        ErrorCode.DUPLICATE_LOGIN ->
                        { // 중복 로그인
                            logoutUseCase.invoke()
                            moveDuplicated()
                        }
                        ErrorCode.INVALID_TOKEN ->
                        { // access or refresh Token이 만료
                        }
                        else -> onSOPOErrorCallback.onFailure(code)
                    }
                }
                is InternalServerException ->
                {
                    val errorCode = ErrorCode.getCode(exception.getErrorResponse().code).apply {
                        message = exception.getErrorResponse().message
                    }

                    SopoLog.e("InternalServerException API Error $errorCode", exception)

                    if(errorCode == ErrorCode.FAIL_TO_SEARCH_PARCEL)
                    {
                        return@CoroutineExceptionHandler onSOPOErrorCallback.onInquiryParcelError(errorCode)
                    }

                    onSOPOErrorCallback.onInternalServerError(errorCode)
                }
                else ->
                {
                    onSOPOErrorCallback.onFailure(ErrorCode.UNKNOWN_ERROR)
                }
            }
        }*/
}