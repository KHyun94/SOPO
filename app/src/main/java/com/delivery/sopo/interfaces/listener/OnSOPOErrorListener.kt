package com.delivery.sopo.interfaces.listener

import com.delivery.sopo.enums.ErrorEnum
import com.delivery.sopo.models.api.ErrorResponse

interface OnSOPOErrorCallback
{
    fun onLoginError(error:ErrorEnum) {}
    fun onSignUpError(error:ErrorEnum) {}
    fun onAuthError(error:ErrorEnum){}
    fun onFailure(error:ErrorEnum)
    fun onInternalServerError(error:ErrorEnum){}
}