package com.delivery.sopo.models.mapper

import com.delivery.sopo.data.repository.local.o_auth.OAuthEntity
import com.delivery.sopo.models.dto.OAuthDTO
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import org.koin.core.KoinComponent
import org.koin.core.inject

object OAuthMapper : KoinComponent
{
    private val userLocalRepo: UserLocalRepository by inject()

    fun entityToObject(oAuth: OAuthEntity): OAuthDTO =
        OAuthDTO(accessToken = oAuth.accessToken, tokenType = oAuth.tokenType, refreshToken = oAuth.refreshToken, expiresIn = oAuth.expiresIn, scope = oAuth.scope, refreshTokenExpiredAt = oAuth.refreshTokenExpiredAt)

    fun objectToEntity(oAuth: OAuthDTO): OAuthEntity =
        OAuthEntity(email = userLocalRepo.getUserId(), accessToken = oAuth.accessToken, tokenType = oAuth.tokenType, refreshToken = oAuth.refreshToken, expiresIn = oAuth.expiresIn, scope = oAuth.scope, refreshTokenExpiredAt = oAuth.refreshTokenExpiredAt)
}