package com.delivery.sopo.data.repository.local.o_auth

import com.delivery.sopo.data.repository.local.o_auth.OAuthEntity

interface OAuthLocalDataSource
{
    fun get(userId : String) : OAuthEntity?
    fun insert(OAuth : OAuthEntity)
    fun update(OAuth : OAuthEntity)
    fun delete(OAuth : OAuthEntity)
}