package com.delivery.sopo.domain.usecase.user.reset

import com.delivery.sopo.data.repositories.user.UserRepository
import com.delivery.sopo.models.user.ResetAuthCode
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class VerifyAuthTokenUseCase(private val userRepository: UserRepository, private val dispatcher: CoroutineDispatcher)
{
    suspend operator fun invoke(authCode: ResetAuthCode) = withContext(dispatcher) {
        return@withContext userRepository.requestVerifyAuthToken(authCode = authCode)
    }
}