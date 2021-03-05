package com.delivery.sopo.networks.call

import com.delivery.sopo.database.room.entity.OauthEntity
import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.models.parcel.ParcelId
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.networks.api.ParcelAPI
import com.delivery.sopo.repository.impl.OauthRepoImpl
import com.delivery.sopo.repository.impl.UserRepoImpl
import com.delivery.sopo.services.network_handler.BaseService
import com.delivery.sopo.services.network_handler.NetworkResult
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.runBlocking
import org.koin.core.KoinComponent
import org.koin.core.inject

object ParcelCall : BaseService(), KoinComponent
{
    val TAG = this.javaClass.simpleName
    val userRepoImpl : UserRepoImpl by inject()
    val oAuthRepoImpl : OauthRepoImpl by inject()
    val email : String
        get() = userRepoImpl.getEmail()

    var parcelAPI : ParcelAPI

    init
    {
        val oauth : OauthEntity?
        runBlocking { oauth = oAuthRepoImpl.get(email = email) }
        SopoLog.d( msg = "토큰 정보 => ${oauth}")

        parcelAPI = NetworkManager.retro(oauth?.accessToken).create(ParcelAPI::class.java)
    }

    suspend fun registerParcel(trackCompany: String, trackNum: String, parcelAlias: String):NetworkResult<APIResult<ParcelId?>>
    {
        val result = parcelAPI.registerParcel(email = email, trackCompany = trackCompany, trackNum = trackNum, parcelAlias = parcelAlias)
        return apiCall(call = {result})
    }

    suspend fun getSingleParcel(parcelId: ParcelId) : NetworkResult<APIResult<Parcel?>>
    {
        val result = parcelAPI.getSingleParcel(email = email, regDt = parcelId.regDt, parcelUid = parcelId.parcelUid)
        return apiCall(call = {result})
    }

    suspend fun requestParcelForRefreshs() : NetworkResult<APIResult<String?>>
    {
        val result = parcelAPI.requestParcelForRefreshs(email = email)
        return apiCall(call = { result })
    }

    suspend fun requestParcelForRefresh(parcelId : ParcelId) : NetworkResult<APIResult<Any?>>
    {
        val result = parcelAPI.requestParcelForRefresh(email = email, regDt = parcelId.regDt, parcelUid = parcelId.parcelUid)
        return apiCall(call = { result })
    }
}