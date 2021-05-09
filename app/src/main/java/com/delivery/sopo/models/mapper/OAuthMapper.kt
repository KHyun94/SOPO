package com.delivery.sopo.models.mapper

import com.delivery.sopo.data.repository.local.o_auth.OAuthEntity
import com.delivery.sopo.models.dto.OAuthDTO
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import org.koin.core.KoinComponent
import org.koin.core.inject

object OAuthMapper : KoinComponent
{
    private val userLocalRepo: UserLocalRepository by inject()
    
    fun objectToEntity(oAuth: OAuthDTO): OAuthEntity =
        OAuthEntity(userLocalRepo.getUserId(), oAuth.accessToken, oAuth.tokenType, oAuth.refreshToken, oAuth.expiresIn, oAuth.scope, oAuth.refreshTokenExpiredAt)
}