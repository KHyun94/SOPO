package com.delivery.sopo.presentation.consts

object IntentConst
{
    object Action
    {
        const val REGISTERED_COMPLETED_PARCEL = "android.intent.action.REGISTERED_COMPLETED_PARCEL"
        const val REGISTERED_ONGOING_PARCEL = "android.intent.action.REGISTERED_ONGOING_PARCEL"
    }

    object Extra
    {
        const val REGISTERED_DATE = "REGISTERED_DATE"
        const val LOCK_STATUS_TYPE = "LOCK_STATUS_TYPE"
    }
}