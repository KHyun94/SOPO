package com.delivery.sopo.consts

sealed class ResultTypeConst
{
    object SuccessType{

    }
    object ErrorType{
        const val ERROR_TYPE_NON = -1
        const val ERROR_TYPE_TOAST = 0
        const val ERROR_TYPE_SNACK_BAR = 1
        const val ERROR_TYPE_DIALOG = 2
        const val ERROR_TYPE_SCREEN = 3
    }
}