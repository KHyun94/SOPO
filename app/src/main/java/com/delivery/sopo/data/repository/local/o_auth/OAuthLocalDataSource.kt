package com.delivery.sopo.data.repository.local.o_auth

import com.delivery.sopo.data.repository.local.o_auth.OAuthEntity
import com.delivery.sopo.models.dto.OAuthDTO

interface OAuthLocalDataSource
{
    fun get(userId : String) : OAuthEntity?
    fun insert(oAuth : OAuthDTO)
    fun update(OAuth : OAuthEntity)
    fun delete(OAuth : OAuthEntity)
}