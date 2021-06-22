package com.delivery.sopo.networks.call

import com.delivery.sopo.data.repository.local.o_auth.OAuthEntity
import com.delivery.sopo.exceptions.APIException
import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.models.parcel.ParcelDTO
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.networks.api.ParcelAPI
import com.delivery.sopo.data.repository.local.o_auth.OAuthLocalRepository
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.models.ParcelRegisterDTO
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

    suspend fun registerParcel(registerDto: ParcelRegisterDTO):NetworkResult<APIResult<Int?>>
    {
        val result = parcelAPI.registerParcel(registerDto = registerDto)
        return apiCall(call = {result})
    }

    suspend fun getSingleParcel(parcelId:Int) : NetworkResult<APIResult<ParcelDTO?>>
    {
        val result = parcelAPI.getSingleParcel(parcelId)
        return apiCall(call = {result})
    }

    suspend fun getOngoingParcels(): NetworkResult<APIResult<List<ParcelDTO>?>>
    {
        val result = parcelAPI.getOngoingParcels(email = email)
        return apiCall(call = {result})
    }


    suspend fun requestParcelForRefreshs() : NetworkResult<APIResult<String?>>
    {
        val result = parcelAPI.requestParcelForRefreshs(email = email)
        return apiCall(call = { result })
    }

    suspend fun requestParcelForRefresh(parcelId : Int) : NetworkResult<APIResult<Unit>>
    {
        val result = parcelAPI.requestParcelForRefresh(parcelId)
        return apiCall(call = { result })
    }


    suspend fun getSingleParcelTest(parcelId:Int): APIResult<ParcelDTO?>
    {
        val result = parcelAPI.getSingleParcel(parcelId)

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