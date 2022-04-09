package com.delivery.sopo.interfaces.listener

import com.delivery.sopo.enums.ErrorEnum
import com.delivery.sopo.models.api.ErrorResponse

interface OnSOPOErrorCallback
{
    fun onLoginError(error:ErrorEnum) {}
    fun onErrorAlreadyRegisteredUser(error:ErrorEnum) {}
    fun onAuthError(error:ErrorEnum){}
    fun onDuplicateError(error: ErrorEnum) {}
    fun onRegisterParcelError(error:ErrorEnum){}
    fun onInquiryParcelError(error:ErrorEnum){}
    fun onFailure(error:ErrorEnum)
    fun onInternalServerError(error:ErrorEnum){}
}