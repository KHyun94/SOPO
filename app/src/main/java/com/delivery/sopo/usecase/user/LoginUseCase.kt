package com.delivery.sopo.usecase.user

import com.delivery.sopo.data.repository.remote.user.UserRemoteRepository

class LoginUseCase(private val userRemoteRepo: UserRemoteRepository)
{
    suspend operator fun invoke(email: String, password: String) {
        userRemoteRepo.requestLogin(email = email, password = password)
        userRemoteRepo.getUserInfo()
    }
}