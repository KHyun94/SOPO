package com.delivery.sopo.data.repositories.user

import com.delivery.sopo.consts.UserTypeConst
import com.delivery.sopo.data.models.JoinInfo
import com.delivery.sopo.data.resources.user.local.UserDataSource
import com.delivery.sopo.data.resources.user.remote.SignUpRemoteDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SignupRepositoryImpl(private val userDataSource: UserDataSource, private val signUpRemoteDataSource: SignUpRemoteDataSource): SignupRepository
{
    override suspend fun signup(userType: String, joinInfo: JoinInfo)
    {
        when(userType)
        {
            UserTypeConst.KAKAO -> signUpRemoteDataSource.requestJoinByKakao(joinInfo)
            UserTypeConst.SELF -> signUpRemoteDataSource.requestJoinBySelf(joinInfo)
        }

        withContext(Dispatchers.Default) {
            userDataSource.setUsername(joinInfo.email)
            userDataSource.setUserPassword(joinInfo.password)
        }
    }
}