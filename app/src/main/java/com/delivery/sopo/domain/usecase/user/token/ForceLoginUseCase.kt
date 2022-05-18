package com.delivery.sopo.domain.usecase.user.token

import com.delivery.sopo.data.repositories.user.UserRepository

class ForceLoginUseCase(private val userRepository: UserRepository)
{
    suspend operator fun invoke() {

        val isExpired = userRepository.checkExpiredTokenWithInWeek()

        if(isExpired)
        {
            userRepository.login()
        }

        userRepository.fetchUserInfo()
    }
}