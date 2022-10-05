package com.delivery.sopo.domain.usecase.user.reset

import com.delivery.sopo.data.repositories.user.UserRepository
import com.delivery.sopo.models.user.ResetAuthCode
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class VerifyAuthTokenUseCase @Inject constructor(private val userRepository: UserRepository)
{
    suspend operator fun invoke(authCode: ResetAuthCode) = withContext(Dispatchers.IO) {
        return@withContext userRepository.requestVerifyAuthToken(authCode = authCode)
    }
}