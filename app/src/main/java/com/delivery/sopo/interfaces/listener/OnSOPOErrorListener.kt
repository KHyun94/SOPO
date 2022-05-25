package com.delivery.sopo.interfaces.listener

import com.delivery.sopo.enums.ErrorCode

interface OnSOPOErrorCallback
{
    fun onLoginError(error:ErrorCode) {}
    fun onAlreadyRegisteredUser(error:ErrorCode) {}
    fun onAuthError(error:ErrorCode){}
    fun onDuplicateError(error: ErrorCode) {}
    fun onRegisterParcelError(error:ErrorCode){}
    fun onInquiryParcelError(error:ErrorCode){}
    fun onFailure(error:ErrorCode)
    fun onInternalServerError(error:ErrorCode){}
}