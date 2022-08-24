package com.delivery.sopo.data.resources.user.remote

import com.delivery.sopo.data.models.JoinInfo
import com.delivery.sopo.data.networks.serivces.SignUpService
import com.delivery.sopo.presentation.services.network_handler.BaseService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SignUpRemoteDataSourceImpl @Inject constructor(private val signUpService: SignUpService, private val dispatcher: CoroutineDispatcher): SignUpRemoteDataSource, BaseService()
{
    override suspend fun requestJoinBySelf(joinInfo: JoinInfo) = withContext(dispatcher) {
        apiCall { signUpService.signUpBySelf(joinInfo) }
    }

    override suspend fun requestJoinByKakao(joinInfo: JoinInfo) = withContext(dispatcher) {
        apiCall { signUpService.signUpByKakao(joinInfo) }
    }
}
