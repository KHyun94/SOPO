package com.delivery.sopo.usecase.user

import com.delivery.sopo.data.repository.JoinRepository
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.data.repository.remote.user.UserRemoteRepository
import com.delivery.sopo.extensions.wrapBodyAliasToHashMap
import com.delivery.sopo.networks.dto.joins.JoinInfo
import com.delivery.sopo.networks.repository.JoinRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SignUpUseCase(private val userLocalRepo:UserLocalRepository, private val joinRepo:JoinRepositoryImpl)
{
    suspend operator fun invoke(joinInfo: JoinInfo)= withContext(Dispatchers.IO){

        joinRepo.requestJoinBySelf(joinInfo)

        withContext(Dispatchers.Default) {
            userLocalRepo.setUserId(joinInfo.email)
            userLocalRepo.setUserPassword(joinInfo.password)
        }
    }
}