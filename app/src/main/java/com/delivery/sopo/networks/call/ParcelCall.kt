package com.delivery.sopo.networks.call

import com.delivery.sopo.data.repository.local.o_auth.OAuthEntity
import com.delivery.sopo.exceptions.APIException
import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.models.parcel.ParcelId
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.networks.api.ParcelAPI
import com.delivery.sopo.networks.dto.parcels.RegisterParcelDTO
import com.delivery.sopo.data.repository.local.o_auth.OAuthLocalRepository
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.services.network_handler.BaseService
import com.delivery.sopo.services.network_handler.NetworkResult
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.runBlocking
import org.koin.core.KoinComponent
import org.koin.core.inject

object ParcelCall : BaseService(), KoinComponent
{
    private val userLocalRepo : UserLocalRepository by inject()
    private val oAuthLocalRepo : OAuthLocalRepository by inject()
    val email : String
        get() = userLocalRepo.getUserId()

    var parcelAPI : ParcelAPI

    init
    {
        val oAuth : OAuthEntity? = runBlocking {  oAuthLocalRepo.get(userId = email) }
        SopoLog.d( msg = "토큰 정보 => ${oAuth}")

        parcelAPI = NetworkManager.retro(oAuth?.accessToken).create(ParcelAPI::class.java)
    }

    suspend fun registerParcel(dto: RegisterParcelDTO):NetworkResult<APIResult<ParcelId?>>
    {
        val result = parcelAPI.registerParcel(dto = dto)
        return apiCall(call = {result})
    }

    suspend fun getSingleParcel(parcelId: ParcelId) : NetworkResult<APIResult<Parcel?>>
    {
        val result = parcelAPI.getSingleParcel(parcelId.regDt, parcelId.parcelUid)
        return apiCall(call = {result})
    }

    suspend fun getOngoingParcels(): NetworkResult<APIResult<MutableList<Parcel>?>>
    {
        val result = parcelAPI.getOngoingParcels(email = email)
        return apiCall(call = {result})
    }


    suspend fun requestParcelForRefreshs() : NetworkResult<APIResult<String?>>
    {
        val result = parcelAPI.requestParcelForRefreshs(email = email)
        return apiCall(call = { result })
    }

    suspend fun requestParcelForRefresh(parcelId : ParcelId) : NetworkResult<APIResult<Any?>>
    {
        val result = parcelAPI.requestParcelForRefresh(parcelId)
        return apiCall(call = { result })
    }


    suspend fun getSingleParcelTest(parcelId: ParcelId): APIResult<Parcel?>
    {
        val result = parcelAPI.getSingleParcel(parcelId.regDt, parcelId.parcelUid)

        when(val apiResult = apiCall(call = { result }))
        {
            is NetworkResult.Success ->
            {
                return apiResult.data
            }
            is NetworkResult.Error ->
            {
                throw apiResult.exception as APIException
            }
        }
    }
}