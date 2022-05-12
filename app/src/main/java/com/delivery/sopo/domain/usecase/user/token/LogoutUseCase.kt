package com.delivery.sopo.domain.usecase.user.token

import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LogoutUseCase(private val userLocalRepo: UserLocalRepository)
{
    operator fun invoke() = CoroutineScope(Dispatchers.Default).launch {
        userLocalRepo.removeUserRepo()
    }
}