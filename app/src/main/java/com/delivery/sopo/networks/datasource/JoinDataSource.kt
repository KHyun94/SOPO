package com.delivery.sopo.networks.datasource

import com.delivery.sopo.models.ErrorResult
import com.delivery.sopo.models.SuccessResult

typealias JoinCallback = (SuccessResult<String?>?, ErrorResult<String?>?) -> Unit
typealias DuplicateCallback = (SuccessResult<Boolean?>?, ErrorResult<Boolean?>?) -> Unit

interface JoinDataSource
{
    suspend fun requestJoinBySelf(email: String, password: String, deviceInfo:String, firebaseUid : String, callback: JoinCallback)
    suspend fun requestJoinByKakao(email: String, password: String, deviceInfo:String, kakaoUid : String, firebaseUid : String, callback: JoinCallback)
    suspend fun requestDuplicatedEmail(email: String, callback: DuplicateCallback)
}