package com.delivery.sopo.enums

import android.view.View

enum class SnackBarEnum
{
    /**
     * 일반 에러 - 유효성 체크
     * 택배 등록 - 버튼
     * 네트워크 에러
     */

    COMMON,
    UPDATE,
    CONFIRM_DELETE,
    CONNECT_NETWORK,
    DISCONNECT_NETWORK,
    ERROR
}

sealed class SnackBarType{
    data class Common(val content: String, val duration: Long): SnackBarType()
    data class Update(val content: String, val duration: Long): SnackBarType()
    data class ConfirmDelete(val content: String, val duration: Long): SnackBarType()
    data class ConnectNetwork(val content: String, val duration: Long): SnackBarType()
    data class DisconnectNetwork(val content: String, val duration: Long): SnackBarType()
    data class Error(val content: String, val duration: Long): SnackBarType()
}