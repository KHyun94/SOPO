package com.delivery.sopo.domain.usecase.user

import com.delivery.sopo.data.repository.user.UserRepository
import com.delivery.sopo.util.SopoLog
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UpdateFCMTokenUseCase(private val userRepository: UserRepository)
{
    suspend operator fun invoke() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if(!task.isSuccessful)
            {
                SopoLog.e("updateFCMToken(...) 실패", task.exception)
                return@addOnCompleteListener
            }

            SopoLog.d("updateFCMToken(...) 성공")
            CoroutineScope(Dispatchers.IO).launch {
                userRepository.updateFCMToken(task.result)
            }
        }
    }
}