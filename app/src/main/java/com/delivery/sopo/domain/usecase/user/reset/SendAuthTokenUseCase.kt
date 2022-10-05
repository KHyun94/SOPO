package com.delivery.sopo.domain.usecase.user.reset

import com.delivery.sopo.data.repositories.user.UserRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SendAuthTokenUseCase @Inject constructor(private val userRepository: UserRepository)
{
    suspend operator fun invoke(username: String): String = withContext(Dispatchers.IO) {
        return@withContext userRepository.requestAuthCodeEmail(email = username)
    }
}