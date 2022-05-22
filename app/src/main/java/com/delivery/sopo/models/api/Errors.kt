package com.delivery.sopo.models.api

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Errors(@SerializedName("errors") val errors: List<Error>): Serializable
