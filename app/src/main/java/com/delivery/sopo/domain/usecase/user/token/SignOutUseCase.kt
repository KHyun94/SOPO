package com.delivery.sopo.domain.usecase.user.token

import com.delivery.sopo.data.repositories.user.UserRepository
import javax.inject.Inject

class SignOutUseCase @Inject constructor(private val userRepository: UserRepository)
{
    suspend operator fun invoke(reason: String){
        userRepository.deleteUser(reason)

    }
}