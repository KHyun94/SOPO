package com.delivery.sopo.domain.usecase.user

import com.delivery.sopo.data.repositories.user.UserRepository

class FetchUserInfoUseCase(private val userRepository: UserRepository)
{
    suspend operator fun invoke(nickname: String)
    {
        userRepository.fetchUserInfo()
    }
}