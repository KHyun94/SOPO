package com.delivery.sopo.models

import com.delivery.sopo.enums.ResponseCode

data class ErrorResult<T>(
    // 에러 코드
    val code: ResponseCode? = null,
    // 에러 메시지
    val errorMsg: String,
    // 에러 타입 0:Toast, 1:SnackBar, 2:Dialog, 3:Screen
    var errorType: Int,
    val data: T? = null,
    val e: Throwable? = null
)
{
    // todo 나중에 sealed class로 변경
    companion object
    {
        const val ERROR_TYPE_NON = -1
        const val ERROR_TYPE_TOAST = 0
        const val ERROR_TYPE_SNACK_BAR = 1
        const val ERROR_TYPE_DIALOG = 2
        const val ERROR_TYPE_SCREEN = 3
    }

    fun ErrorResult<T>.isNotNull() = this != null
}