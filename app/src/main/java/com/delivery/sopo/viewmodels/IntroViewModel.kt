package com.delivery.sopo.viewmodels

import androidx.lifecycle.ViewModel
import com.delivery.sopo.models.base.BaseViewModel
import kotlinx.coroutines.CoroutineExceptionHandler

class IntroViewModel:BaseViewModel()
{
    override val exceptionHandler: CoroutineExceptionHandler
        get() = CoroutineExceptionHandler { coroutineContext, throwable ->  }
}