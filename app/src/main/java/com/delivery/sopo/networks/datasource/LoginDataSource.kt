package com.delivery.sopo.networks.datasource

import com.delivery.sopo.models.ErrorResult
import com.delivery.sopo.models.SuccessResult

typealias CustomTokenCallback = (SuccessResult<String?>?, ErrorResult<String?>?) -> Unit

interface LoginDataSource
{
    suspend fun requestCustomToken(email :String, deviceInfo :String, joinType :String, kakaoUid :String, callback: CustomTokenCallback)
}