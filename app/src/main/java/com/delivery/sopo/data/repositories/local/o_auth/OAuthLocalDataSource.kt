package com.delivery.sopo.data.repositories.local.o_auth

import com.delivery.sopo.data.database.room.entity.AuthTokenEntity
import com.delivery.sopo.models.dto.OAuthToken

interface OAuthLocalDataSource
{
    suspend fun get(userId : String) : OAuthToken
    fun insert(oAuth : OAuthToken)
    fun update(authToken : AuthTokenEntity)
    fun delete(authToken : AuthTokenEntity)
}