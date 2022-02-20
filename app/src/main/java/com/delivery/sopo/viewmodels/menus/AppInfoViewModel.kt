package com.delivery.sopo.viewmodels.menus

import androidx.lifecycle.*
import com.delivery.sopo.enums.ErrorEnum
import com.delivery.sopo.exceptions.UserExceptionHandler
import com.delivery.sopo.interfaces.listener.OnSOPOErrorCallback
import com.delivery.sopo.models.base.BaseViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers

class AppInfoViewModel : BaseViewModel() {
    private val onSOPOErrorCallback = object: OnSOPOErrorCallback
    {
        override fun onFailure(error: ErrorEnum) { }
    }

    override val exceptionHandler: CoroutineExceptionHandler by lazy {
        UserExceptionHandler(Dispatchers.Main, onSOPOErrorCallback)
    }
}