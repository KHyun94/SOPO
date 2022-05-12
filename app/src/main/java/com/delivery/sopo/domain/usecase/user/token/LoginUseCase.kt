package com.delivery.sopo.domain.usecase.user.token

import com.delivery.sopo.data.repository.user.UserRepository

class LoginUseCase(private val userRepository: UserRepository)
{
    suspend operator fun invoke(userName: String, password: String) {
        userRepository.login(userName = userName, password = password)
        userRepository.fetchUserInfo()
    }
}