package com.delivery.sopo.domain.usecase.user.reset

import com.delivery.sopo.data.repositories.user.UserRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class SendAuthTokenUseCase(private val userRepository: UserRepository, private val dispatcher: CoroutineDispatcher)
{
    suspend operator fun invoke(username: String): String = withContext(dispatcher) {
        return@withContext userRepository.requestAuthCodeEmail(email = username)
    }
}