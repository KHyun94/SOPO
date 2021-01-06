package com.delivery.sopo.mapper

import com.delivery.sopo.database.room.entity.OauthEntity
import com.delivery.sopo.models.OauthResult

object OauthMapper
{
    fun entityToObject(oauth: OauthEntity) = OauthResult(oauth.accessToken, oauth.tokenType, oauth.refreshToken, oauth.expiresIn, oauth.scope)
    fun objectToEntity(email : String, oauth: OauthResult) = OauthEntity(email, oauth.accessToken, oauth.tokenType, oauth.refreshToken, oauth.expiresIn, oauth.scope)
}