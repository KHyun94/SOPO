package com.delivery.sopo.usecase

import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.data.repository.remote.user.UserRemoteRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UpdateNicknameUseCase(private val userRemoteRepo: UserRemoteRepository, private val userLocalRepo: UserLocalRepository)
{
    suspend operator fun invoke(nickname: String) = withContext(Dispatchers.Main) {
        userRemoteRepo.updateNickname(nickname = nickname)
        withContext(Dispatchers.Default) { userLocalRepo.setNickname(nickname = nickname) }
    }
}