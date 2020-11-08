package com.delivery.sopo.viewmodels.menus

import android.util.Log
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.enums.MenuEnum
import com.delivery.sopo.extensions.MutableLiveDataExtension.popItem
import com.delivery.sopo.extensions.MutableLiveDataExtension.pushItem
import com.delivery.sopo.models.SopoJsonPatch
import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.networks.api.UserAPI
import com.delivery.sopo.networks.dto.JsonPatchDto
import com.delivery.sopo.repository.impl.ParcelRepoImpl
import com.delivery.sopo.repository.impl.TimeCountRepoImpl
import com.delivery.sopo.repository.impl.UserRepoImpl
import com.delivery.sopo.util.OtherUtil
import com.delivery.sopo.util.SopoLog
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class MenuViewModel(private val userRepoImpl: UserRepoImpl,
                    private val parcelRepoImpl: ParcelRepoImpl,
                    private val timeCountRepoImpl: TimeCountRepoImpl
) : ViewModel(), LifecycleObserver
{
    private val TAG = "LOG.SOPO${this.javaClass.simpleName}"

    private val _cntOfSoonListItem = parcelRepoImpl.getSoonDataCntLiveData()
    val cntOfSoonListItem: LiveData<Int>
        get() = _cntOfSoonListItem

    private val _cntOfOngoingListItem = parcelRepoImpl.getOngoingDataCntLiveData()
    val cntOfOngoingListItem: LiveData<Int>
        get() = _cntOfOngoingListItem

    private val _cntOfCompleteListItem = timeCountRepoImpl.getSumOfCountLiveData()
    val cntOfCompleteListItem: LiveData<Int>
        get() = _cntOfCompleteListItem

    private val _userEmail = MutableLiveData<String>()
    val userEmail: LiveData<String>
        get() = _userEmail
    //todo 닉네임 업데이트

    private val _userNickname  = MutableLiveData<String>()
    val userNickname : LiveData<String>
        get() = _userNickname

    private val _menu = MutableLiveData<MenuEnum>()
    val menu: LiveData<MenuEnum>
        get() = _menu

    private val _viewStack = MutableLiveData<Stack<MenuEnum>>()
    val viewStack: LiveData<Stack<MenuEnum>>
        get() = _viewStack

    val isUpdate = MutableLiveData<Boolean>()

    init {
        _userEmail.value = userRepoImpl.getEmail()
        _userNickname.value = userRepoImpl.getUserNickname()
        SopoLog.d("Menu 닉네임 => ${userNickname.value}")
        _viewStack.value = Stack()
        isUpdate.value = false

    }

    fun pushView(menu: MenuEnum){
        _viewStack.pushItem(menu)
        _menu.value = menu
    }

    fun popView(): Boolean{
        return try {
            _viewStack.popItem()
            _viewStack.value?.also {
                if(!it.empty()){
                    _menu.value = it.peek()
                }
            }
            true
        }
        catch (e: EmptyStackException){
            Log.e(TAG, "STACK IS ALREADY EMPTY!!, you try to pop item even if stack is already empty!!")
            false
        }
    }

    fun onUpdateClicked()
    {
        SopoLog.d("닉네임 변경 클릭")
        isUpdate.value = true
    }

    fun updateUserNickname(nickname : String)
    {
        val jsonPatchList = mutableListOf<SopoJsonPatch>()
        jsonPatchList.add(SopoJsonPatch("replace", "/nickName", nickname))

        NetworkManager.privateRetro.create(UserAPI::class.java)
            .patchUser(
                email = userRepoImpl.getEmail(),
                jwt = null,
                jsonPatch = JsonPatchDto(jsonPatchList)
            )
            .enqueue(object : Callback<APIResult<String?>>{
                override fun onFailure(call: Call<APIResult<String?>>, t: Throwable)
                {
                    SopoLog.d("에러 $t")
                }

                override fun onResponse(
                    call: Call<APIResult<String?>>,
                    response: Response<APIResult<String?>>
                )
                {
                    if(response.code() == 200)
                    {
                        SopoLog.d("닉네임 변경 => ${response.body()}")
                        userRepoImpl.setUserNickname(nickname)
                        _userNickname.value = nickname
                    }
                    else
                        SopoLog.d("닉네임 변경 => ${response.errorBody()}")
                }

            })
    }
}