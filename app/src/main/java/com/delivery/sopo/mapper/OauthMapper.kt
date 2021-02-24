package com.delivery.sopo.mapper

import com.delivery.sopo.database.room.entity.OauthEntity
import com.delivery.sopo.extensions.toDate
import com.delivery.sopo.models.OauthResult
import com.delivery.sopo.repository.impl.UserRepoImpl
import com.delivery.sopo.util.DateUtil
import org.koin.core.KoinComponent
import org.koin.core.inject

object OauthMapper : KoinComponent
{
    private val userRepoImpl: UserRepoImpl by inject()

    fun entityToObject(Oauth: OauthEntity) = OauthResult(Oauth.accessToken, Oauth.tokenType, Oauth.refreshToken, Oauth.expiresIn, Oauth.scope)

    fun objectToEntity(oauth: OauthResult): OauthEntity
    {
        val expiredInMilliSeconds = (oauth.expiresIn.toDouble()).toInt() * 1000
        val milliSeconds = DateUtil.getIntToMilliSeconds(expiredInMilliSeconds)
        val afterMilliSeconds = System.currentTimeMillis() + milliSeconds

        val date = afterMilliSeconds.toDate()

        return OauthEntity(userRepoImpl.getEmail(), oauth.accessToken, oauth.tokenType, oauth.refreshToken, date, oauth.scope)
    }
}