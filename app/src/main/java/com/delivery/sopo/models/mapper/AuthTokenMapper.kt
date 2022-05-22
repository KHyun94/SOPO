package com.delivery.sopo.models.mapper

import com.delivery.sopo.data.database.room.entity.AuthTokenEntity
import com.delivery.sopo.data.models.AuthToken
import com.delivery.sopo.util.DateUtil

object AuthTokenMapper
{
    fun entityToObject(entity: AuthTokenEntity): AuthToken.Info = with(entity) { AuthToken.Info(grantType = grantType, userToken = userToken, accessToken = accessToken, refreshToken = refreshToken, expireAt = expireAt) }
    fun objectToEntity(dto: AuthToken.Info): AuthTokenEntity
        {
            val expireAt = DateUtil.changeDateFormat(dto.expireAt, DateUtil.DATE_TIME_TYPE_AUTH_EXPIRED, DateUtil.DATE_TIME_TYPE_DEFAULT)?:""
            return with(dto) { AuthTokenEntity(grantType = grantType, userToken = userToken, accessToken = accessToken, refreshToken = refreshToken, expireAt = expireAt) }
        }
}