package com.delivery.sopo.models.mapper

import com.delivery.sopo.data.repository.local.o_auth.OAuthEntity
import com.delivery.sopo.models.dto.OAuthToken
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import org.koin.core.KoinComponent
import org.koin.core.inject

object OAuthMapper : KoinComponent
{
    private val userLocalRepo: UserLocalRepository by inject()

    fun entityToObject(oAuth: OAuthEntity): OAuthToken =
        OAuthToken(accessToken = oAuth.accessToken, tokenType = oAuth.tokenType, refreshToken = oAuth.refreshToken, expiresIn = oAuth.expiresIn, scope = oAuth.scope, refreshTokenExpiredAt = oAuth.refreshTokenExpiredAt)

    fun objectToEntity(oAuth: OAuthToken): OAuthEntity =
        OAuthEntity(email = userLocalRepo.getUserId(), accessToken = oAuth.accessToken, tokenType = oAuth.tokenType, refreshToken = oAuth.refreshToken, expiresIn = oAuth.expiresIn, scope = oAuth.scope, refreshTokenExpiredAt = oAuth.refreshTokenExpiredAt)
}