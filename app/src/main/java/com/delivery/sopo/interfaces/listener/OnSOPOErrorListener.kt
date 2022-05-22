package com.delivery.sopo.interfaces.listener

import com.delivery.sopo.enums.ErrorEnum

interface OnSOPOErrorCallback
{
    fun onLoginError(error:ErrorEnum) {}
    fun onAlreadyRegisteredUser(error:ErrorEnum) {}
    fun onAuthError(error:ErrorEnum){}
    fun onDuplicateError(error: ErrorEnum) {}
    fun onRegisterParcelError(error:ErrorEnum){}
    fun onInquiryParcelError(error:ErrorEnum){}
    fun onFailure(error:ErrorEnum)
    fun onInternalServerError(error:ErrorEnum){}
}