package com.delivery.sopo.data.repositories.user

import com.delivery.sopo.data.models.JoinInfo

interface SignupRepository
{
    suspend fun signup(userType: String, joinInfo: JoinInfo)
}