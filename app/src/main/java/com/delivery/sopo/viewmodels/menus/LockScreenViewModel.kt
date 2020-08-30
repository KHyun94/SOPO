package com.delivery.sopo.viewmodels.menus

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.lang.Exception

class LockScreenViewModel : ViewModel()
{
    private val TAG = "LOG.SOPO${this.javaClass.simpleName}"

    var lockPassword = MutableLiveData<String>()
    var firstCheck = MutableLiveData<Boolean>()
    var finalCheck = MutableLiveData<Boolean>()
    var passwordIsNotMatched = MutableLiveData<Boolean>()
    var isFinish = MutableLiveData<Boolean>()
    var verifyCnt = MutableLiveData<Int>()


    private var firstCheckPassword = ""

    init{
        lockPassword.value = ""
        firstCheck.value = false
        finalCheck.value = false
        passwordIsNotMatched.value = false
        isFinish.value = false
        verifyCnt.value = 0
    }

    fun setlockPassword(num: Int){

        lockPassword.value?.let {
            if (it.length < 4){
                lockPassword.value = "${it}$num"
            }
        }
        lockPassword.value?.let {
            if (it.length == 4 && firstCheckPassword == ""){
                firstCheckPassword = it
                lockPassword.value = ""
                verifyCnt.value = 1
            }
            else if(it.length == 4){

                //[다시 한번 입력해주세요.]에서 틀렸을 경우, 첨부터 다시 시도.
               if(firstCheckPassword != it){
                    lockPassword.value = ""
                    firstCheckPassword = ""
                    verifyCnt.value = 2
                }
                //[다시 한번 입력해주세요.]에서 일치한 경우, 저장 후 종료.
                //todo: 해당 비밀번호를 저장해야함.
                else{
                    verifyCnt.value = 3
                }
            }
        }
    }

    fun eraselockPassword(){
        val currentPassword = lockPassword.value

        currentPassword?.let {
            if(it.isNotEmpty()){
                val substring = it.substring(IntRange(0, (it.lastIndex-1)))
                lockPassword.value = substring
            }
        }
    }
}