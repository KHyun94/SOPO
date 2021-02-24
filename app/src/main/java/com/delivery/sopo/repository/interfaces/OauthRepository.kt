package com.delivery.sopo.repository.interfaces

import com.delivery.sopo.database.room.entity.OauthEntity

interface OauthRepository
{
    fun get(email : String) : OauthEntity?
    fun insert(Oauth : OauthEntity)
    fun update(Oauth : OauthEntity)
    fun delete(Oauth : OauthEntity)
}