package com.delivery.sopo.domain.usecase.user

import com.delivery.sopo.data.repositories.user.UserRepository

class FetchUserInfoUseCase(private val userRepository: UserRepository)
{
    val nickname: String
    get() = userRepository.getUserDataSource().getNickname()

    suspend operator fun invoke(nickname: String)
    {
        userRepository.fetchUserInfo()
    }
}