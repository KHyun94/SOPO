package com.delivery.sopo.viewmodels.registesrs

import android.os.Handler
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.bindings.FocusChangeCallback
import com.delivery.sopo.enums.InfoEnum
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.models.CourierItem
import com.delivery.sopo.repository.impl.CourierRepoImpl
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.views.widget.CustomEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class RegisterStep1ViewModel(private val courierRepoImpl: CourierRepoImpl) : ViewModel()
{
    val TAG = this.javaClass.simpleName

    var wayBilNum = MutableLiveData<String?>()
    var courier = MutableLiveData<CourierItem?>()
    // 가져온 클립보드 문자열
    var clipBoardWords = MutableLiveData<String>()

    var moveFragment = MutableLiveData<String>()

    val errorMsg = MutableLiveData<String>()

    private var _navigator = MutableLiveData<String>()
    val navigator: LiveData<String>
        get() = _navigator

    val validates = mutableMapOf<InfoEnum, Boolean>()

    private var _validateError = MutableLiveData<Pair<InfoEnum, Boolean>>()
    val validateError: LiveData<Pair<InfoEnum, Boolean>>
        get() = _validateError

    private val _focus = MutableLiveData<Triple<View, Boolean, InfoEnum>>()
    val focus: MutableLiveData<Triple<View, Boolean, InfoEnum>>
        get() = _focus

    val focusChangeCallback: FocusChangeCallback = FocusChangeCallback@{ v, hasFocus, type->
        SopoLog.i("${type.NAME} >>> $hasFocus")
        Handler().postDelayed(Runnable { _focus.value = (Triple(v, hasFocus, type)) }, 50)
    }

    init
    {
        validates[InfoEnum.WAYBILL_NUMBER] = false

        moveFragment.value = ""
        clipBoardWords.value = ""
    }

    fun onMove2ndStepClicked()
    {
        moveFragment.value = TabCode.REGISTER_STEP2.NAME
    }

    fun onMove3rdStepClicked()
    {
        val courierCode = courier.value!!.courierCode
        val wayBilNum = wayBilNum.value
        CoroutineScope(Dispatchers.Default).launch {
            val courierEntity = courierRepoImpl.getCourierEntityWithCode(courierCode)
            val minLen = courierEntity.minLen
            val maxLen = courierEntity.maxLen

            withContext(Dispatchers.Main){
                if(wayBilNum?.length in minLen..maxLen || minLen == 0)
                {
                    moveFragment.postValue(TabCode.REGISTER_STEP3.NAME)
                }
                else
                {
                    errorMsg.postValue("운송장 번호를 다시 확인해주세요.\n(택배사마다 정해진 길이가 안맞아서 발생)")
                }
            }

        }


    }

    // clipBoardWord(클립보드에 저장된 값)을 wayBilNum(택배 운송장 번호) 입력 란으로 삽입
    fun onPasteClicked()
    {
        wayBilNum.value = clipBoardWords.value
    }

}

