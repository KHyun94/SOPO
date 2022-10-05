package com.delivery.sopo.domain.usecase.user.token

import com.delivery.sopo.data.repositories.user.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class LogoutUseCase @Inject constructor(private val userRepository: UserRepository)
{
    operator fun invoke() = CoroutineScope(Dispatchers.Default).launch {
        // TODO 삭제 로직
    }
}