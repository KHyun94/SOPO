package com.delivery.sopo.models.base

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.delivery.sopo.enums.ErrorCode
import com.delivery.sopo.enums.NetworkStatus
import com.delivery.sopo.exceptions.InternalServerException
import com.delivery.sopo.exceptions.SOPOApiException
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.ui_util.BottomNotificationBar
import com.delivery.sopo.util.ui_util.OnSnackBarClickListener
import com.orhanobut.logger.Logger
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.core.KoinComponent

abstract class BaseViewModel: ViewModel(), KoinComponent
{
    var onSnackClickListener: Pair<CharSequence, OnSnackBarClickListener<Unit>>? = null

    private val _isClickEvent = MutableLiveData<Boolean>()
    val isClickEvent: LiveData<Boolean> = _isClickEvent

    private var isCheckNetwork: Boolean = false

    val networkStatus = MutableStateFlow<NetworkStatus>(NetworkStatus.Default)

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

    val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
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

    protected val scope: CoroutineScope = (viewModelScope + job + exceptionHandler)

    protected open fun handlerAPIException(exception: SOPOApiException){
        if(exception.code == ErrorCode.DUPLICATE_LOGIN)
        {
            _isDuplicated.postValue(true)
            scope.cancel("중복 로그인으로 인한 모든 프로세스 종료")
            return
        }
    }
    protected open fun handlerInternalServerException(exception: InternalServerException){}
    protected open fun handlerException(exception: Exception){}

    fun checkEventStatus(checkNetwork: Boolean = false, delayMillisecond: Long = 100, event: () -> Unit)
    {
        if(_isClickEvent.value == true)
        {
            return Logger.d("Already Click Event Start")
        }

        _isClickEvent.postValue(true)

        if(checkNetwork)
        {
            /*checkNetworkStatus().also { value ->
                if(!value)
                {
                    _isClickEvent.postValue(false)
                    return
                }
            }*/
        }
        else
        {
            _isClickEvent.postValue(false)
        }

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

    private var isInitNetwork: Boolean = false

    fun checkNetworkStatus(callback:(Boolean)->Unit) = viewModelScope.launch{

        if(!isInitNetwork && networkStatus.value != NetworkStatus.NotConnect)
        {
            isInitNetwork = true
            return@launch
        }

        networkStatus.collect { status ->

            if(status is NetworkStatus.Default) return@collect

            when(status)
            {
                is NetworkStatus.Cellular, is NetworkStatus.Wifi ->
                {
                    callback(true)
                }
                is NetworkStatus.NotConnect ->
                {
                    callback(false)
                }
                is NetworkStatus.Default -> return@collect
            }
        }

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

    override fun onCleared()
    {
        super.onCleared()
        _isClickEvent.value = false
        scope.cancel()
    }
}