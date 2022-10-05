package com.delivery.sopo.presentation.models.enums

import com.delivery.sopo.models.parcel.Parcel
import java.io.Serializable

sealed class RegisterNavigation: Serializable
{
    object Init: RegisterNavigation()
    data class Next(val nextStep: String, val parcel: Parcel.Register): RegisterNavigation()
    data class Complete(val parcel: Parcel.Common): RegisterNavigation()
}