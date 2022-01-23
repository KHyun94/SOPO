package com.delivery.sopo.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.models.base.BaseViewModel
import kotlinx.coroutines.CoroutineExceptionHandler

class IntroViewModel:BaseViewModel()
{
    override val exceptionHandler: CoroutineExceptionHandler
        get() = CoroutineExceptionHandler { coroutineContext, throwable ->  }

    private val _navigator = MutableLiveData<String>()
    val navigator: LiveData<String>
        get() = _navigator

    fun setNavigator(navigator: String){
        _navigator.postValue(navigator)
    }
}