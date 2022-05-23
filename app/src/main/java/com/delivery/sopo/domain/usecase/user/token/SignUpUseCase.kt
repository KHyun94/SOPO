package com.delivery.sopo.domain.usecase.user.token

import com.delivery.sopo.data.models.JoinInfo
import com.delivery.sopo.data.repositories.user.SignupRepository

class SignUpUseCase(private val signupRepository: SignupRepository)
{
    suspend operator fun invoke(joinInfo: JoinInfo, userType: String)
    {
        signupRepository.signup(userType = userType, joinInfo = joinInfo)
    }
}