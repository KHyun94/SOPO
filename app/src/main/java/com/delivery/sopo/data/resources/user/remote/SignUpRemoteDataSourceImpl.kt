package com.delivery.sopo.data.resources.user.remote

import com.delivery.sopo.data.networks.NetworkManager
import com.delivery.sopo.data.models.JoinInfo
import com.delivery.sopo.data.networks.serivces.SignUpService
import com.delivery.sopo.enums.NetworkEnum
import com.delivery.sopo.services.network_handler.BaseService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class SignUpRemoteDataSourceImpl(private val dispatcher: CoroutineDispatcher): SignUpRemoteDataSource, BaseService()
{
    private val signUpService: SignUpService by lazy { NetworkManager.setLoginMethod(NetworkEnum.PRIVATE_LOGIN, SignUpService::class.java) }

    override suspend fun requestJoinBySelf(joinInfo: JoinInfo) = withContext(dispatcher) {
        apiCall { signUpService.signUpBySelf(joinInfo) }
    }

    override suspend fun requestJoinByKakao(joinInfo: JoinInfo) = withContext(dispatcher) {
        apiCall { signUpService.signUpByKakao(joinInfo) }
    }
}
