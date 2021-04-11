package com.delivery.sopo.mapper

import com.delivery.sopo.database.room.entity.OauthEntity
import com.delivery.sopo.models.OauthResult
import com.delivery.sopo.repository.impl.UserRepoImpl
import org.koin.core.KoinComponent
import org.koin.core.inject

object OauthMapper : KoinComponent
{
    private val userRepoImpl: UserRepoImpl by inject()

    fun entityToObject(oAuth: OauthEntity) = OauthResult(oAuth.accessToken, oAuth.tokenType, oAuth.refreshToken, oAuth.expiresIn, oAuth.scope, oAuth.refreshTokenExpiredAt)
    fun objectToEntity(oAuth: OauthResult): OauthEntity = OauthEntity(userRepoImpl.getEmail(), oAuth.accessToken, oAuth.tokenType, oAuth.refreshToken, oAuth.expiresIn, oAuth.scope, oAuth.refreshTokenExpiredAt)
}