package com.delivery.sopo.domain.usecase.user

import com.delivery.sopo.data.repository.user.UserRepository

class UpdateNicknameUseCase(private val userRepository: UserRepository)
{
    suspend operator fun invoke(nickname: String)
    {
        userRepository.updateNickname(nickname = nickname)
        userRepository.fetchUserInfo()
    }
}