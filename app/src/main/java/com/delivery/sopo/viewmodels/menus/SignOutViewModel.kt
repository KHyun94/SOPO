package com.delivery.sopo.viewmodels.menus

import android.view.View
import android.widget.CheckBox
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.R
import com.delivery.sopo.networks.repository.OAuthNetworkRepo
import com.delivery.sopo.util.SopoLog
import kotlinx.android.synthetic.main.sign_out_view.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SignOutViewModel: ViewModel()
{
    var preCheckBox: AppCompatCheckBox? = null
    var currentCheckBox: AppCompatCheckBox? = null

    val result = MutableLiveData<String>()
    fun onCheckClicked(v: View, message: String?)
   {
        v.requestFocusFromTouch()

        // TODO 키보드 내리기

        currentCheckBox = v as AppCompatCheckBox

        if(currentCheckBox != preCheckBox) preCheckBox?.isChecked = false

        if(message == null || message == "")
        {
            SopoLog.d("sign out reason is null or empty")
            preCheckBox = currentCheckBox
            return
        }

        SopoLog.d("Sign Out Message >>> $message")

        CoroutineScope(Dispatchers.Main).launch {
            requestSignOut(message)
        }

        preCheckBox = currentCheckBox
    }

    suspend fun requestSignOut(reason: String)
    {
//        val res = OAuthNetworkRepo.requestSignOut(reason)
//
//        if(!res.result)
//        {
//            SopoLog.e("계정 탈퇴 >>> ${res.code} / ${res.message}")
//            return
//        }
//
//        SopoLog.d("계정 탈퇴 완료")
        result.postValue("reason >>> $reason")
    }

}