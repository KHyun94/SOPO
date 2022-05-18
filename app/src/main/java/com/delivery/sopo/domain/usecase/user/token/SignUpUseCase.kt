package com.delivery.sopo.domain.usecase.user.token

import com.delivery.sopo.consts.UserTypeConst
import com.delivery.sopo.data.repositories.local.user.UserLocalRepository
import com.delivery.sopo.data.networks.dto.joins.JoinInfo
import com.delivery.sopo.data.resources.user.remote.SignUpRemoteDataSourceImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SignUpUseCase(private val userLocalRepo: UserLocalRepository, private val joinRepo: SignUpRemoteDataSourceImpl)
{
    suspend operator fun invoke(joinInfo: JoinInfo, userType: String)
    {
        when(userType)
        {
            UserTypeConst.KAKAO -> joinRepo.requestJoinByKakao(joinInfo)
            UserTypeConst.SELF -> joinRepo.requestJoinBySelf(joinInfo)
        }

        withContext(Dispatchers.Default) {
            userLocalRepo.setUserId(joinInfo.email)
            userLocalRepo.setUserPassword(joinInfo.password)
        }
    }
}