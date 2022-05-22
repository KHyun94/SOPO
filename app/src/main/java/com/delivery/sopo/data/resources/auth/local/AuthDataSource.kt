package com.delivery.sopo.data.resources.auth.local

import com.delivery.sopo.data.models.AuthToken

interface AuthDataSource
{
    suspend fun get(): AuthToken.Info
    suspend fun insert(token: AuthToken.Info)
    suspend fun update(token: AuthToken.Info)
    suspend fun delete(token: AuthToken.Info)
}