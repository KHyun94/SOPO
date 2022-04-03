package com.delivery.sopo.usecase.user

import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.data.repository.remote.user.UserRemoteRepository
import com.delivery.sopo.extensions.wrapBodyAliasToHashMap

class SignOutUseCase(private val userLocalRepo:UserLocalRepository, private val userRemoteRepo:UserRemoteRepository)
{
    suspend operator fun invoke(reason: String){

        userRemoteRepo.requestSignOut(reason)
        userLocalRepo.removeUserRepo()
    }
}