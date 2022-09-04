package com.delivery.sopo.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.delivery.sopo.models.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class IntroViewModel @Inject constructor():BaseViewModel()
{
    private val _navigator = MutableLiveData<String>()
    val navigator: LiveData<String>
        get() = _navigator

    fun postNavigator(navigator: String){
        _navigator.postValue(navigator)
    }
}