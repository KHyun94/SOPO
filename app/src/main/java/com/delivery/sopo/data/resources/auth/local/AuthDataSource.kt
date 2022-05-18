package com.delivery.sopo.data.resources.auth.local

import com.delivery.sopo.models.dto.OAuthToken

interface AuthDataSource
{
    suspend fun get(userName: String): OAuthToken
    suspend fun insert(token: OAuthToken)
    suspend fun update(token: OAuthToken)
    suspend fun delete(token: OAuthToken)
}