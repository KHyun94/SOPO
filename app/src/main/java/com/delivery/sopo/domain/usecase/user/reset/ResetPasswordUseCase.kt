package com.delivery.sopo.domain.usecase.user.reset

import com.delivery.sopo.data.repositories.user.UserRepository
import com.delivery.sopo.models.user.ResetPassword
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class ResetPasswordUseCase(private val userRepository: UserRepository, private val dispatcher: CoroutineDispatcher)
{
    suspend operator fun invoke(resetPassword: ResetPassword) = withContext(dispatcher) {
        return@withContext userRepository.updatePassword(resetPassword = resetPassword)
    }
}