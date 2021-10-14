package com.delivery.sopo.viewmodels.signup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.data.repository.remote.user.UserRemoteRepository
import com.delivery.sopo.exceptions.APIBetaException
import com.delivery.sopo.exceptions.InternalServerException
import com.delivery.sopo.models.ResponseResult
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SignUpCompleteViewModel(private val userLocalRepo: UserLocalRepository, private val userRemoteRepo: UserRemoteRepository): ViewModel()
{
    private val _error = MutableLiveData<Pair<Int, String>>()
    val error: LiveData<Pair<Int, String>>
        get() = _error

    val handler = CoroutineExceptionHandler { context, exception ->
        when(exception)
        {
            is APIBetaException ->
            {
                // TODO Error Set
            }
            is InternalServerException ->
            {
                _error.postValue(Pair(500, exception.message))
            }
        }
    }

    val email = MutableLiveData<String>().also {
        it.postValue(userLocalRepo.getUserId())
    }

    private val _navigator = MutableLiveData<String>()
    val navigator: LiveData<String>
        get() = _navigator

    private val scope: CoroutineScope = viewModelScope

    fun onCompleteClicked()
    {
        requestLogin(userLocalRepo.getUserId(), userLocalRepo.getUserPassword())
    }

    private fun requestLogin(email: String, password: String) = scope.launch(Dispatchers.IO) {

        userRemoteRepo.requestLogin(email, password)

        val userInfo = userRemoteRepo.getUserInfo()

        if(userInfo.nickname == "")
        {
            _navigator.postValue(NavigatorConst.TO_UPDATE_NICKNAME)
            return@launch
        }

        _navigator.postValue(NavigatorConst.TO_MAIN)
    }
}
