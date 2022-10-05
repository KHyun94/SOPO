package com.delivery.sopo.domain.usecase.user

import com.delivery.sopo.data.repositories.user.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdateNicknameUseCase @Inject constructor(private val userRepository: UserRepository)
{
    suspend fun getUserNickname() = withContext(Dispatchers.Default){
        return@withContext userRepository.getUserDataSource().getNickname()
    }

    suspend operator fun invoke(nickname: String)
    {
        userRepository.updateNickname(nickname = nickname)
        userRepository.fetchUserInfo()
    }
}