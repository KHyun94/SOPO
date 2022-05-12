package com.delivery.sopo.domain.usecase.user.token

import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.data.repository.remote.user.UserRemoteRepository
import com.delivery.sopo.data.repository.user.UserRepository

class SignOutUseCase(private val userRepository: UserRepository)
{
    suspend operator fun invoke(reason: String){
        userRepository.deleteUser(reason)

    }
}