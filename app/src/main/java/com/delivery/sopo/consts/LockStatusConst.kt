package com.delivery.sopo.consts

sealed class LockStatusConst
{
    object SET{
        const val VERIFY_STATUS = "SET_VERIFY_STATUS"
        const val CONFIRM_STATUS = "SET_CONFIRM_STATUS"
        const val FAILURE_STATUS = "SET_FAILURE_STATUS"
    }

    object VERIFY{
        const val CONFIRM_STATUS = "VERIFY_CONFIRM_STATUS"
        const val FAILURE_STATUS = "VERIFY_FAILURE_STATUS"
    }

    object AUTH{
        const val CONFIRM_STATUS = "AUTH_CONFIRM_STATUS"
        const val FAILURE_STATUS = "AUTH_FIRST_FAILURE_STATUS"
    }

}