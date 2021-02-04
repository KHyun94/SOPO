package com.delivery.sopo.networks.call

import com.delivery.sopo.database.room.entity.OauthEntity
import com.delivery.sopo.models.UserDetail
import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.networks.api.UserAPI
import com.delivery.sopo.repository.impl.OauthRepoImpl
import com.delivery.sopo.repository.impl.UserRepoImpl
import com.delivery.sopo.services.network_handler.BaseService
import com.delivery.sopo.services.network_handler.NetworkResult
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.runBlocking
import org.koin.core.KoinComponent
import org.koin.core.inject

object UserCall : BaseService(), KoinComponent
{
    val TAG = this.javaClass.simpleName
    val userRepoImpl : UserRepoImpl by inject()
    val oauthRepoImpl : OauthRepoImpl by inject()
    var userAPI : UserAPI

    init
    {
        val oauth : OauthEntity?
        runBlocking { oauth = oauthRepoImpl.get(userRepoImpl.getEmail()) }
        SopoLog.d(tag = TAG, msg = "토큰 정보 => ${oauth}")
        userAPI = NetworkManager.retro(oauth?.accessToken).create(UserAPI::class.java)
    }

    suspend fun getUserInfoWithToken() : NetworkResult<APIResult<UserDetail?>>
    {
        val result = userAPI.getUserInfoWithToken()
        return apiCall(call = {result})
    }
}